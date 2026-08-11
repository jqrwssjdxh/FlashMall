package com.flashmall.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flashmall.dto.ProductCreateDTO;
import com.flashmall.dto.ProductQueryDTO;
import com.flashmall.dto.ProductStatusDTO;
import com.flashmall.dto.ProductUpdateDTO;
import com.flashmall.entity.Product;


public interface ProductService {


    Product getProduct(Long id);

    Page<Product> list(ProductQueryDTO queryDTO);

    void add(ProductCreateDTO dto);

    void update(Long id, ProductUpdateDTO dto);

    void updateStatus(Long id, ProductStatusDTO dto);

    void restoreStock(Long productId, Integer count);
}