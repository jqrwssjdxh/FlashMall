package com.flashmall.constant;

public enum ResultCode{
    SUCCESS(200,"操作成功"),
    OUT_OF_STOCK(201,"库存不足"),
    ORDER_NOT_FOUND(202, "订单不存在"),
    USER_ALREADY_EXISTS(1003, "用户名已存在"),
    USER_NOT_FOUND(1004, "用户不存在");
    private final int code;
    private final String msg;
ResultCode(int code, String msg) {
    this.code = code;
    this.msg = msg;
}
public  Integer getCode() {
    return code;
}
public  String getMessage() {
    return msg;
}

}

