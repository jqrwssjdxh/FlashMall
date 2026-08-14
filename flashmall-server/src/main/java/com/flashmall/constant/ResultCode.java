package com.flashmall.constant;

public enum ResultCode {

    SUCCESS(200, "操作成功"),

    OUT_OF_STOCK(201, "库存不足"),

    ORDER_NOT_FOUND(202, "订单不存在"),

    PRODUCT_NOT_FOUND(203, "商品不存在"),

    ORDER_CREATE_FAIL(204, "订单创建失败"),

    USER_ALREADY_EXISTS(1003, "用户名已存在"),

    USER_NOT_FOUND(1004, "用户不存在"),

    CART_NOT_FOUND(1005, "购物车商品不存在"),
    ORDER_STATUS_ERROR(1006,"订单状态错误"),

    PARAM_ERROR(1007, "参数错误"),
    REPEAT_SUBMIT(1008, "请勿重复提交订单");

    private final int code;

    private final String msg;


    ResultCode(int code, String msg) {

        this.code = code;

        this.msg = msg;

    }


    public Integer getCode() {

        return code;

    }


    public String getMessage() {

        return msg;

    }


}