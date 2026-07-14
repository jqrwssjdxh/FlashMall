package com.flashmall.controller;

import com.flashmall.constant.ResultCode;
import com.flashmall.exception.BusinessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        throw new BusinessException(ResultCode.OUT_OF_STOCK);
    }

}