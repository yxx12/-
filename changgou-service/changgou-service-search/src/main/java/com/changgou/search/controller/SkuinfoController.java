package com.changgou.search.controller;

import com.changgou.search.service.SkuInfoService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/search")
public class SkuinfoController {
    @Autowired(required = false)
    private SkuInfoService skuInfoService;

    /**
     * 将正常状态的库存信息保存到ES索引库中
     *
     * @return
     */
    @GetMapping("/import")
    public Result importData() {
        skuInfoService.importSkuInfoToEs();
        return new Result(true, StatusCode.OK, "数据导入成功");
    }

    /**
     * 商品检索
     */
    @GetMapping
    public Map<String, Object> search(@RequestParam(required = false) Map<String, String> searchMap) {
        Map<String, Object> resultMap = skuInfoService.search(searchMap);
        return resultMap;
    }

}
