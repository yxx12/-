package com.changgou.order.service;

import com.changgou.order.pojo.OrderItem;

import java.util.List;

public interface CartService {
    /**
     * 购物车添加商品
     *
     * @param skuId
     * @param num
     * @param username
     * @return
     */
    void add(Long skuId, Integer num, String username);

    /**
     * 查询购物车商品列表
     *
     * @param username
     * @return
     */
    List<OrderItem> list(String username);
}
