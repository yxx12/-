package com.changgou.goods.service;

import com.changgou.goods.pojo.Category;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface CategoryService {

    /**
     * 根据父节点d查询
     */
    List<Category> findByParentId(Integer id);

    /**
     * 查询所有
     */
    List<Category> findAll();

    Category findById(Integer id);

    void add(Category category);

    void deleteById(Integer id);

    void update(Category category);

    List<Category> findByCategory(Category category);

    PageInfo<Category> fingPage(Integer page, Integer size);

    PageInfo<Category> findPage(Category category, Integer page, Integer size);
    /**
     * 根据父id查询商品分类
     * @param parentId
     * @return
     */
    List<Category> findListByParentId(Integer parentId);
}
