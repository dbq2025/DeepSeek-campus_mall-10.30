package com.gdufe.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdufe.mall.common.exception.BizException;
import com.gdufe.mall.common.result.PageResult;
import com.gdufe.mall.common.result.ResultCode;
import com.gdufe.mall.common.security.SecurityUtil;
import com.gdufe.mall.common.util.PageUtil;
import com.gdufe.mall.common.util.SpecUtil;
import com.gdufe.mall.dto.OrderCreateDTO;
import com.gdufe.mall.dto.OrderItemInputDTO;
import com.gdufe.mall.entity.Cart;
import com.gdufe.mall.entity.OrderItem;
import com.gdufe.mall.entity.Orders;
import com.gdufe.mall.entity.Product;
import com.gdufe.mall.mapper.CartMapper;
import com.gdufe.mall.mapper.OrderItemMapper;
import com.gdufe.mall.mapper.OrdersMapper;
import com.gdufe.mall.mapper.ProductMapper;
import com.gdufe.mall.service.OrderService;
import com.gdufe.mall.vo.CreateOrderVO;
import com.gdufe.mall.vo.OrderDetailVO;
import com.gdufe.mall.vo.OrderItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String DEFAULT_CANCEL_REASON = "用户取消";

    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final CartMapper cartMapper;

    /** 一行待下单内容：来源可能是购物车行，也可能是「立即购买」的入参 */
    private record Line(Long productId, Integer quantity, String specText) {
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateOrderVO create(OrderCreateDTO dto) {
        Long userId = SecurityUtil.currentUserId();
        List<Line> lines = new ArrayList<>();
        List<Long> usedCartIds = new ArrayList<>();

        if (Boolean.TRUE.equals(dto.getFromCart())) {
            if (dto.getCartIds() == null || dto.getCartIds().isEmpty()) {
                throw new BizException(ResultCode.PARAM_ERROR, "请选择要结算的商品");
            }
            List<Long> ids = dto.getCartIds().stream().filter(Objects::nonNull).distinct().toList();
            List<Cart> carts = cartMapper.selectList(new LambdaQueryWrapper<Cart>()
                    .eq(Cart::getUserId, userId)
                    .in(Cart::getId, ids));
            // 含他人行/已删除行时整单拒绝，绝不部分下单（契约边界 #17）
            if (carts.size() != ids.size()) {
                throw new BizException(ResultCode.NOT_FOUND, "部分购物车商品已失效，请刷新后重试");
            }
            for (Cart c : carts) {
                lines.add(new Line(c.getProductId(), c.getQuantity(), c.getSpecText()));
                usedCartIds.add(c.getId());
            }
        } else {
            if (dto.getItems() == null || dto.getItems().isEmpty()) {
                throw new BizException(ResultCode.PARAM_ERROR, "请选择要购买的商品");
            }
            for (OrderItemInputDTO it : dto.getItems()) {
                lines.add(new Line(it.getProductId(), it.getQuantity(), it.getSpecText()));
            }
        }

        Set<Long> productIds = lines.stream().map(Line::productId).collect(Collectors.toSet());
        Map<Long, Product> productMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            for (Product p : productMapper.selectBatchIds(productIds)) {
                productMap.put(p.getId(), p);
            }
        }
        if (productMap.size() != productIds.size()) {
            throw new BizException(ResultCode.NOT_FOUND, "部分商品已不存在");
        }

        BigDecimal total = BigDecimal.ZERO;
        // TreeMap：按商品 ID 升序扣库存，避免并发下单互相死锁
        Map<Long, Integer> deduct = new TreeMap<>();
        List<OrderItem> itemEntities = new ArrayList<>();

        for (Line line : lines) {
            Product p = productMap.get(line.productId());
            if (p.getStatus() == null || p.getStatus() != 1) {
                throw new BizException(ResultCode.PRODUCT_OFF_SHELF, "商品已下架：" + p.getName());
            }
            // 规格再校验一次：购物车行可能来自旧规格定义
            String spec = SpecUtil.normalize(p.getSpecJson(), line.specText());
            if (p.getStock() == null || p.getStock() < line.quantity()) {
                throw new BizException(ResultCode.STOCK_NOT_ENOUGH, "库存不足：" + p.getName());
            }
            BigDecimal lineTotal = p.getPrice()
                    .multiply(BigDecimal.valueOf(line.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            total = total.add(lineTotal);
            deduct.merge(p.getId(), line.quantity(), Integer::sum);

            OrderItem item = new OrderItem();
            item.setProductId(p.getId());
            item.setProductName(p.getName());
            item.setProductImage(p.getMainImage());
            item.setPrice(p.getPrice());
            item.setQuantity(line.quantity());
            item.setSpecText(spec);
            item.setTotalAmount(lineTotal);
            itemEntities.add(item);
        }
        total = total.setScale(2, RoundingMode.HALF_UP);

        // 扣库存：条件更新兜底并发，影响行数为 0 即整单回滚
        for (Map.Entry<Long, Integer> e : deduct.entrySet()) {
            if (productMapper.deductStock(e.getKey(), e.getValue()) == 0) {
                throw new BizException(ResultCode.STOCK_NOT_ENOUGH,
                        "库存不足：" + productMap.get(e.getKey()).getName());
            }
        }

        Orders order = new Orders();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setPayAmount(total);
        order.setStatus(0);
        order.setReceiverName(dto.getReceiverName().trim());
        order.setReceiverPhone(dto.getReceiverPhone().trim());
        order.setReceiverAddress(dto.getReceiverAddress().trim());
        order.setRemark(StringUtils.hasText(dto.getRemark()) ? dto.getRemark().trim() : null);
        ordersMapper.insert(order);

        for (OrderItem item : itemEntities) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }
        if (!usedCartIds.isEmpty()) {
            cartMapper.deleteBatchIds(usedCartIds);
        }

        log.info("创建订单 orderNo={} userId={} amount={}", order.getOrderNo(), userId, total);
        return new CreateOrderVO(order.getId(), order.getOrderNo(), total);
    }

    @Override
    public PageResult<OrderItemVO> page(Integer status, Integer page, Integer size) {
        Long userId = SecurityUtil.currentUserId();
        LambdaQueryWrapper<Orders> w = new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId);
        if (status != null) {
            w.eq(Orders::getStatus, status);
        }
        w.orderByDesc(Orders::getCreateTime).orderByDesc(Orders::getId);

        Page<Orders> result = ordersMapper.selectPage(PageUtil.of(page, size), w);
        Map<Long, List<OrderItem>> itemMap = loadItems(
                result.getRecords().stream().map(Orders::getId).toList());
        List<OrderItemVO> records = new ArrayList<>(result.getRecords().size());
        for (Orders o : result.getRecords()) {
            records.add(OrderAssembler.toListItem(o,
                    itemMap.getOrDefault(o.getId(), Collections.emptyList()), false));
        }
        PageResult<OrderItemVO> pr = PageResult.of(result);
        pr.setRecords(records);
        return pr;
    }

    @Override
    public OrderDetailVO detail(Long id) {
        Orders o = requireVisible(id);
        Map<Long, List<OrderItem>> itemMap = loadItems(List.of(id));
        OrderDetailVO vo = OrderAssembler.toDetail(o,
                itemMap.getOrDefault(id, Collections.emptyList()), false);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(Long id) {
        Long userId = SecurityUtil.currentUserId();
        int rows = ordersMapper.pay(id, userId);
        if (rows == 0) {
            Orders o = ordersMapper.selectById(id);
            if (o == null) {
                throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
            }
            if (!o.getUserId().equals(userId)) {
                throw new BizException(ResultCode.FORBIDDEN);
            }
            if (Objects.equals(o.getStatus(), 1)) {
                // 重复支付：幂等成功，不重复写 pay_time（契约边界 #19）
                return;
            }
            throw new BizException(ResultCode.STATUS_NOT_ALLOWED, "当前订单状态不可支付");
        }
        // 支付成功才累加销量（D1），影响行数为 1 保证只加一次
        for (OrderItem it : loadItems(List.of(id)).getOrDefault(id, Collections.emptyList())) {
            productMapper.increaseSales(it.getProductId(), it.getQuantity());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id, String reason) {
        Long userId = SecurityUtil.currentUserId();
        String finalReason = StringUtils.hasText(reason) ? reason.trim() : DEFAULT_CANCEL_REASON;
        int rows = ordersMapper.cancelByUser(id, userId, finalReason);
        if (rows == 0) {
            Orders o = ordersMapper.selectById(id);
            if (o == null) {
                throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
            }
            if (!o.getUserId().equals(userId)) {
                throw new BizException(ResultCode.FORBIDDEN);
            }
            if (Objects.equals(o.getStatus(), 4)) {
                // 重复取消：幂等成功，且不再回补库存（契约边界 #20）
                return;
            }
            throw new BizException(ResultCode.STATUS_NOT_ALLOWED, "已支付订单不能直接取消");
        }
        restoreStock(id);
        log.info("取消订单 id={} reason={}", id, finalReason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long id) {
        Long userId = SecurityUtil.currentUserId();
        int rows = ordersMapper.confirmByUser(id, userId);
        if (rows == 0) {
            Orders o = ordersMapper.selectById(id);
            if (o == null) {
                throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
            }
            if (!o.getUserId().equals(userId)) {
                throw new BizException(ResultCode.FORBIDDEN);
            }
            if (Objects.equals(o.getStatus(), 3)) {
                return;
            }
            throw new BizException(ResultCode.STATUS_NOT_ALLOWED, "只有待收货订单可以确认收货");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void timeoutCancel(Long id) {
        int rows = ordersMapper.cancelBySystem(id, "超时未付款");
        if (rows == 1) {
            restoreStock(id);
            log.info("超时自动取消订单 id={}", id);
        }
    }

    /** 订单可见性：本人或管理员；他人订单一律 2003，不泄露存在性 */
    private Orders requireVisible(Long id) {
        Orders o = ordersMapper.selectById(id);
        if (o == null) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
        if (!o.getUserId().equals(SecurityUtil.currentUserId()) && !SecurityUtil.isAdmin()) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        return o;
    }

    private void restoreStock(Long orderId) {
        for (OrderItem it : loadItems(List.of(orderId)).getOrDefault(orderId, Collections.emptyList())) {
            productMapper.restoreStock(it.getProductId(), it.getQuantity());
        }
    }

    private Map<Long, List<OrderItem>> loadItems(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .in(OrderItem::getOrderId, orderIds)
                .orderByAsc(OrderItem::getId));
        Map<Long, List<OrderItem>> map = new HashMap<>();
        for (OrderItem it : items) {
            map.computeIfAbsent(it.getOrderId(), k -> new ArrayList<>()).add(it);
        }
        return map;
    }

    /** 单号 = 秒级时间戳 + 6 位随机，长度 20，满足 varchar(32) 与唯一索引 */
    private String generateOrderNo() {
        return LocalDateTime.now().format(ORDER_NO_FMT)
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }
}
