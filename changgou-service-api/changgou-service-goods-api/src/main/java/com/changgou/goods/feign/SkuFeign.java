package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "goods")  //指  功能所在eruake的注册名
@RequestMapping("/sku")
public interface SkuFeign {
    /**
     * 下单后更新库存
     * (个人觉得应该是付款后更新库存，或者发货后)
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/decr")
     Result decr(@RequestParam(value = "skuId") Long skuId,@RequestParam(value = "num") Integer num);
    /**
     * 查询状态商品列表
     * @param status
     * @return
     */
    @GetMapping("/findSkusByStatus/{status}")
    public Result<List<Sku>> findSkusByStatus(@PathVariable(value = "status") String status);

    /**
     * 查询库存信息
     *
     * @param sku
     * @return
     */
    @PostMapping(value = "/search")
    Result<List<Sku>> findList(@RequestBody(required = false) Sku sku);

    /***
     * 根据ID查询Sku数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Sku> findById(@PathVariable(value = "id") Long id);
}
