package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired(required = false)
    private SkuFeign skuFeign;

    @Autowired(required = false)
    private SpuFeign spuFeign;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override

    public void add(Long skuId, Integer num, String username) {
        Result<Sku> skuResult = skuFeign.findById(skuId);
        if (skuResult != null) {
            Sku sku = skuResult.getData();
            Spu spu = spuFeign.findById(sku.getSpuId()).getData();
            //有了spu，sku，将商品信息加入购物车
            OrderItem orderItem = googs2OrderItem(sku, spu, num);
            redisTemplate.boundHashOps("Cart_" + username).put(skuId, orderItem);
        }
    }

    @Override
    public List<OrderItem> list(String username) {
        List list = redisTemplate.boundHashOps("Cart_" + username).values();
        return list;
    }

    /**
     * 将商品信息加入购物车
     *
     * @param sku
     * @param spu
     * @param num
     * @return
     */
    private OrderItem googs2OrderItem(Sku sku, Spu spu, Integer num) {
        OrderItem orderItem = new OrderItem();
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        orderItem.setSpuId(spu.getId());
        orderItem.setSkuId(sku.getId());
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(sku.getPrice() * num);
        orderItem.setPayMoney(sku.getPrice() * num);
        orderItem.setImage(sku.getImage());
        orderItem.setWeight(sku.getWeight() * num);
        orderItem.setPostFee(10);
        return orderItem;
    }
}
