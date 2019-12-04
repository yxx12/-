package com.changgou.goods.service;

import com.changgou.goods.pojo.Brand;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface BrandService {
    List<Brand> findAll();

    Brand findById(Integer id);

    void add(Brand brand);

    void deleteById(Integer id);

    void update(Brand brand);

    //条件查询
    List<Brand> findList(Brand brand);

    //分页查询
    PageInfo<Brand> findPage(Integer pageNum, Integer size);

    //按照条件分页查询
    PageInfo<Brand> findPageByBrand(Integer pageNum, Integer size,Brand brand);

    /**
     * 根据cateId查询品牌列表
     * @param cateId
     * @return
     */
    List<Brand> findListByCateId(Integer cateId);
}
