package com.flashmall.exception;
import com.flashmall.constant.ResultCode;

public class BusinessException extends RuntimeException {
    private final ResultCode resultCode;
    public BusinessException(ResultCode resultCode){
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }
    public ResultCode getResultCode() {
        return resultCode;
    }

}
