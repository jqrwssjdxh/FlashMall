package com.flashmall.common;
import com.flashmall.constant.ResultCode;

public class Result <T>{
    private Integer code;
    private String message;
    private T data;
    private Result() {}
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
    public static <T> Result <T> success(T data){
        Result<T> result = new Result<>();
        result.code = ResultCode.SUCCESS.getCode();
        result.message = ResultCode.SUCCESS.getMessage();
        result.data = data;
        return result;
    }
    public static<T> Result<T> success(){
        return success(null);
    }
    public static <T> Result <T> fail(ResultCode resultCode){
        Result<T> result = new Result<>();
        result.code =resultCode.getCode();
        result.message = resultCode.getMessage();
        result.data = null;
        return result;
    }
    public static <T> Result <T> fail(Integer code, String message){
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        result.data = null;
        return result;
    }

}
