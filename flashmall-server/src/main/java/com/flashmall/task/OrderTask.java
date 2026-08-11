package com.flashmall.task;


import com.flashmall.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class OrderTask {


    private final OrderService orderService;


    /**
     * 每分钟扫描一次超时订单
     */
    @Scheduled(fixedRate = 60000)
    public void cancelTimeoutOrders(){


        orderService.cancelTimeoutOrders();


    }


}