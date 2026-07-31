package com.flashmall.controller;


import com.flashmall.common.Result;
import com.flashmall.entity.Product;
import com.flashmall.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {


    private final ProductService productService;


    @GetMapping("/{id}")
    public Result<Product> getProduct(
            @PathVariable Long id
    ){

        Product product =
                productService.getProduct(id);


        return Result.success(product);

    }

}