package com.flashmall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.flashmall.mapper")
public class FlashMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                FlashMallApplication.class,args);
    }

}
