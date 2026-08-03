package com.flashmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flashmall.entity.Order;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface OrderMapper extends BaseMapper<Order> {

}