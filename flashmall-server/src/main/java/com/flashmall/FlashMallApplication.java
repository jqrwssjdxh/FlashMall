package com.flashmall;

import com.flashmall.constant.ResultCode;
import com.flashmall.exception.BusinessException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FlashMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlashMallApplication.class, args);

        throw new BusinessException(ResultCode.OUT_OF_STOCK);
    }

}
