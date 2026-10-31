package com.gdufe.mall.controller;

import com.gdufe.mall.common.result.PageResult;
import com.gdufe.mall.common.result.Result;
import com.gdufe.mall.dto.ShipDTO;
import com.gdufe.mall.service.AdminOrderService;
import com.gdufe.mall.vo.OrderDetailVO;
import com.gdufe.mall.vo.OrderItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    /** ORD-01 订单分页（含 userName） */
    @GetMapping
    public Result<PageResult<OrderItemVO>> page(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return Result.ok(adminOrderService.page(orderNo, status, page, size));
    }

    /** ORD-02 订单详情 */
    @GetMapping("/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        return Result.ok(adminOrderService.detail(id));
    }

    /** ORD-03 发货 */
    @PutMapping("/{id}/ship")
    public Result<Void> ship(@PathVariable Long id,
                             @Valid @RequestBody(required = false) ShipDTO dto) {
        adminOrderService.ship(id, dto == null ? null : dto.getTrackingNo());
        return Result.ok();
    }

    /** ORD-04 代确认收货 */
    @PutMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        adminOrderService.confirm(id);
        return Result.ok();
    }
}
