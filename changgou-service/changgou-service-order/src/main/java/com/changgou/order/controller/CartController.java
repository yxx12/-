package com.changgou.order.controller;

import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import entity.StatusCode;
import entity.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 购物车
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 购物车添加商品
     *
     * @param skuId
     * @param num
     * @param
     * @return
     */
    @RequestMapping("/add")
    public Result add(Long skuId, Integer num) {
        String username = TokenDecode.getUserInfo().get("username");
        cartService.add(skuId, num, username);
        return new Result(true, StatusCode.OK, "购物车添加成功");
    }

    /**
     * 查询购物车商品列表
     *
     * @return
     */
    @GetMapping("/list")
    public Result list() {
        String username = TokenDecode.getUserInfo().get("username");
        List<OrderItem> list = cartService.list(username);
        return new Result(true, StatusCode.OK, "购物车列表查询成功", list);
    }

}
