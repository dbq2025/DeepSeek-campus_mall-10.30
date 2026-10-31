package com.gdufe.mall.task;

import com.gdufe.mall.mapper.OrdersMapper;
import com.gdufe.mall.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 待付款订单超时自动取消。
 * 注意：这里通过注入的 OrderService 代理调用（而非 this 方法），@Transactional 才会生效。
 * 单条失败只记日志，不阻断其他订单——一单异常不能让整批停摆。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrdersMapper ordersMapper;
    private final OrderService orderService;

    @Value("${mall.order.timeout-minutes:30}")
    private int timeoutMinutes;

    @Value("${mall.order.timeout-scan-limit:100}")
    private int scanLimit;

    @Value("${mall.order.timeout-enabled:true}")
    private boolean enabled;

    @Scheduled(fixedDelay = 300_000L, initialDelay = 60_000L)
    public void cancelTimeoutOrders() {
        if (!enabled) {
            return;
        }
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(timeoutMinutes);
        List<Long> ids = ordersMapper.selectTimeoutIds(deadline, scanLimit);
        if (ids.isEmpty()) {
            return;
        }
        log.info("扫描到 {} 个超时未付款订单", ids.size());
        for (Long id : ids) {
            try {
                orderService.timeoutCancel(id);
            } catch (Exception e) {
                log.warn("超时取消订单失败 id={}：{}", id, e.getMessage());
            }
        }
    }
}
