package com.changgou.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.CategoryFeign;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.item.service.PageService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageServiceImpl implements PageService {
    @Autowired(required = false)
    private SkuFeign skuFeign;

    @Autowired(required = false)
    private SpuFeign spuFeign;

    @Autowired(required = false)
    private CategoryFeign categoryFeign;

    @Autowired(required = false)
    private TemplateEngine templateEngine;
    //配置中有生成页面的路径
    @Value("${pagepath}")
    private String pagepath;

    /**
     * 传入 spuId生成静态页面
     *
     * @param spuId
     */
    @Override
    public void createHtml(Long spuId) {
        try {
            //获取页面数据
            Map<String, Object> dataModel = getDataModel(spuId);
            //构建模型数据
            Context context = new Context();
            context.setVariables(dataModel);
            //指定静态页面生成的位置
            File dir = new File(pagepath);
            if (!dir.exists()) {
                dir.mkdirs();   // 目录为空，创建
            }
            File dest = new File(dir, spuId + ".html");
            // 生成页面
            PrintWriter writer = new PrintWriter(dest, "UTF-8");

            templateEngine.process("item", context, writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }




    private Map<String, Object> getDataModel(Long spuId) {
        Map<String, Object> dataModel = new HashMap<>();
        //商品公共信息
        Spu spu = spuFeign.findById(spuId).getData();
        dataModel.put("spu", spu);
        //商品库存信息
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        Result<List<Sku>> skuResult = skuFeign.findList(sku);
        dataModel.put("skuList", skuResult.getData());
        //商品分类信息(三级类别)
        Result<Category> category1Result = categoryFeign.findById(spu.getCategory1Id());
        Result<Category> category2Result = categoryFeign.findById(spu.getCategory2Id());
        Result<Category> category3Result = categoryFeign.findById(spu.getCategory3Id());
        dataModel.put("category1", category1Result.getData());
        dataModel.put("category2", category2Result.getData());
        dataModel.put("category3", category3Result.getData());
        //商品小图片信息
        String[] imageList = spu.getImages().split(",");
        dataModel.put("imageList", imageList);
        //商品规格
        Map<String, String> specificationList = JSON.parseObject(spu.getSpecItems(), Map.class);
        dataModel.put("specificationList", specificationList);

        return dataModel;
    }


}
