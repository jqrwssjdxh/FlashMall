package com.flashmall.controller;


import com.flashmall.common.Result;
import com.flashmall.dto.OrderCreateDTO;
import com.flashmall.entity.Order;
import com.flashmall.service.OrderService;
import com.flashmall.vo.OrderDetailVO;
import com.flashmall.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor//自动生成包含所有final（或@NonNull）字段的构造函数
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<Order> createOrder(
            @Valid @RequestBody OrderCreateDTO dto
    ){


        Order order =
                orderService.createOrder(
                        dto.getProductId(),
                        dto.getQuantity()
                );


        return Result.success(order);

    }

    @GetMapping("/my")
    public Result<List<OrderVO>> getMyOrders(){
        return Result.success(
                orderService.getMyOrders()
        );

    }
    @GetMapping("/{id}")
    public Result<OrderDetailVO> getDetail(
            @PathVariable Long id
    ){

        return Result.success(
                orderService.getOrderDetail(id)
        );

    }
    @PutMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id) {
        orderService.pay(id);
        return Result.success();
    }
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return Result.success();
    }
}