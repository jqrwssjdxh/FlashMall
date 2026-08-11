package com.flashmall.service;


public interface StockService {


    /**
     * 扣减库存
     *
     * @param productId 商品id
     * @param count 数量
     * @return 是否成功
     */
    boolean decreaseStock(Long productId, Integer count);

    void initStock(Long productId,Integer stock);

    void restoreStock(Long productId, Integer count);

}