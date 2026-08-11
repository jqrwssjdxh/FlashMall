package com.flashmall.utils;

public class OrderStatusUtil {


    public static String getName(Integer status){


        return switch (status){

            case 0 -> "待支付";

            case 1 -> "已支付";

            case 2 -> "已发货";

            case 3 -> "已完成";

            case 4 -> "已取消";

            default -> "未知状态";

        };

    }

}