package com.gdufe.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdufe.mall.entity.Product;
import com.gdufe.mall.entity.User;
import com.gdufe.mall.mapper.OrdersMapper;
import com.gdufe.mall.mapper.ProductMapper;
import com.gdufe.mall.mapper.UserMapper;
import com.gdufe.mall.service.StatService;
import com.gdufe.mall.vo.OrderStatusCountVO;
import com.gdufe.mall.vo.SalesTrendVO;
import com.gdufe.mall.vo.StatsOverviewVO;
import com.gdufe.mall.vo.TopProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {

    private static final String[] STATUS_TEXT = {"待付款", "待发货", "待收货", "已完成", "已取消"};

    private final OrdersMapper ordersMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    @Override
    public StatsOverviewVO overview() {
        StatsOverviewVO vo = new StatsOverviewVO();
        vo.setTodayOrderCount(toInt(ordersMapper.countTodayOrders()));
        vo.setTodaySalesAmount(scale2(ordersMapper.sumTodaySales()));
        vo.setPendingShipmentCount(toInt(ordersMapper.countPendingShipment()));
        Long productCount = productMapper.selectCount(null);
        vo.setTotalProductCount(toInt(productCount == null ? 0L : productCount));
        Long userCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, "USER"));
        vo.setTotalUserCount(toInt(userCount == null ? 0L : userCount));
        return vo;
    }

    @Override
    public SalesTrendVO sales(int days) {
        int d = Math.min(Math.max(days, 1), 30);
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(d - 1L);

        Map<String, BigDecimal> amountByDate = new HashMap<>();
        for (Map<String, Object> row : ordersMapper.sumSalesByDay(start.atStartOfDay())) {
            Object day = pick(row, "d");
            Object amt = pick(row, "amt");
            if (day == null) {
                continue;
            }
            String key = String.valueOf(day);
            if (day instanceof java.sql.Date sd) {
                key = sd.toLocalDate().toString();
            } else if (day instanceof LocalDate ld) {
                key = ld.toString();
            }
            BigDecimal value = amt instanceof BigDecimal bd
                    ? bd : new BigDecimal(String.valueOf(amt == null ? "0" : amt));
            amountByDate.put(key, value);
        }

        SalesTrendVO vo = new SalesTrendVO();
        for (int i = 0; i < d; i++) {
            String date = start.plusDays(i).toString();
            vo.getDates().add(date);
            vo.getAmounts().add(scale2(amountByDate.get(date)).toPlainString());
        }
        return vo;
    }

    @Override
    public List<OrderStatusCountVO> orderStatus() {
        Map<Integer, Long> counts = new HashMap<>();
        for (Map<String, Object> row : ordersMapper.countByStatus()) {
            Object st = pick(row, "status");
            Object cnt = pick(row, "cnt");
            if (st == null) {
                continue;
            }
            counts.put(Integer.valueOf(String.valueOf(st)),
                    cnt == null ? 0L : Long.valueOf(String.valueOf(cnt)));
        }
        List<OrderStatusCountVO> list = new ArrayList<>(STATUS_TEXT.length);
        for (int i = 0; i < STATUS_TEXT.length; i++) {
            list.add(new OrderStatusCountVO(i, STATUS_TEXT[i],
                    toInt(counts.getOrDefault(i, 0L))));
        }
        return list;
    }

    @Override
    public List<TopProductVO> topProducts(int limit) {
        int size = Math.min(Math.max(limit, 1), 20);
        Page<Product> page = productMapper.selectPage(new Page<>(1, size),
                new LambdaQueryWrapper<Product>()
                        .orderByDesc(Product::getSales)
                        .orderByDesc(Product::getId));
        List<TopProductVO> list = new ArrayList<>(page.getRecords().size());
        for (Product p : page.getRecords()) {
            list.add(new TopProductVO(p.getId(), p.getName(),
                    p.getSales() == null ? 0 : p.getSales()));
        }
        return list;
    }

    private BigDecimal scale2(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private Integer toInt(long value) {
        return (int) Math.min(value, Integer.MAX_VALUE);
    }

    /** MyBatis 返回的 Map key 大小写取决于驱动，这里做大小写无关取值 */
    private Object pick(Map<String, Object> row, String key) {
        for (Map.Entry<String, Object> e : row.entrySet()) {
            if (e.getKey().equalsIgnoreCase(key)) {
                return e.getValue();
            }
        }
        return null;
    }
}
