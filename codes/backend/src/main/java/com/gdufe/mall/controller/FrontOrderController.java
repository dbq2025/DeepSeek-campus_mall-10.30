package com.gdufe.mall.controller;

import com.gdufe.mall.common.result.PageResult;
import com.gdufe.mall.common.result.Result;
import com.gdufe.mall.dto.CancelDTO;
import com.gdufe.mall.dto.OrderCreateDTO;
import com.gdufe.mall.service.OrderService;
import com.gdufe.mall.vo.CreateOrderVO;
import com.gdufe.mall.vo.OrderDetailVO;
import com.gdufe.mall.vo.OrderItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/front/orders")
@RequiredArgsConstructor
public class FrontOrderController {

    private final OrderService orderService;

    /** FORD-01 创建订单（购物车下单 / 立即购买） */
    @PostMapping
    public Result<CreateOrderVO> create(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.ok(orderService.create(dto));
    }

    /** FORD-02 我的订单分页 */
    @GetMapping
    public Result<PageResult<OrderItemVO>> page(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return Result.ok(orderService.page(status, page, size));
    }

    /** FORD-03 订单详情 */
    @GetMapping("/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        return Result.ok(orderService.detail(id));
    }

    /** FORD-04 支付（模拟） */
    @PostMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id) {
        orderService.pay(id);
        return Result.ok();
    }

    /** FORD-05 取消订单 */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id,
                               @Valid @RequestBody(required = false) CancelDTO dto) {
        orderService.cancel(id, dto == null ? null : dto.getReason());
        return Result.ok();
    }

    /** FORD-06 确认收货 */
    @PutMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        orderService.confirm(id);
        return Result.ok();
    }
}
