package com.flashmall.constant;


import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum OrderStatus {


    WAIT_PAY(0,"待支付"),


    PAID(1,"已支付"),


    SHIPPED(2,"已发货"),


    FINISHED(3,"已完成"),


    CANCELLED(4,"已取消");


    private final Integer code;


    private final String desc;



    public static OrderStatus fromCode(Integer code){


        for(OrderStatus status : values()){


            if(status.code.equals(code)){

                return status;

            }

        }


        throw new RuntimeException("非法订单状态");

    }


}