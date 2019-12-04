package com.changgou.goods.service.impl;

import com.changgou.goods.dao.CategoryMapper;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.service.CategoryService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired(required = false)
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> findByParentId(Integer id) {
        Category category = new Category();
        category.setParentId(id);
        Example exampl = createExampl(category);
        return categoryMapper.selectByExample(exampl);
    }

    @Override
    public List<Category> findAll() {
        return categoryMapper.selectAll();
    }

    @Override
    public Category findById(Integer id) {
        return categoryMapper.selectByPrimaryKey(id);
    }

    @Override
    public void add(Category category) {
        categoryMapper.insertSelective(category);
    }

    @Override
    public void deleteById(Integer id) {
        categoryMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void update(Category category) {
        categoryMapper.updateByPrimaryKeySelective(category);
    }

    @Override
    public List<Category> findByCategory(Category category) {
        Example exampl = createExampl(category);
        List<Category> categories = categoryMapper.selectByExample(exampl);
        return categories;
    }

    @Override
    public PageInfo<Category> fingPage(Integer page, Integer size) {
        PageHelper.startPage(page, size);
        List<Category> categories = categoryMapper.selectAll();
        return new PageInfo<>(categories);
    }

    @Override
    public PageInfo<Category> findPage(Category category, Integer page, Integer size) {
        PageHelper.startPage(page, size);
        Example exampl = createExampl(category);
        List<Category> categories = categoryMapper.selectByExample(exampl);
        return new PageInfo<>(categories);
    }
    /**
     * 根据父id查询商品分类
     * @param parentId
     * @return
     */
    @Override
    public List<Category> findListByParentId(Integer parentId) {
        Category category=new Category();
        category.setParentId(parentId);
        List<Category> list = categoryMapper.select(category);
        return list;
    }

    /**
     * 构建查询条件
     *
     * @param category
     * @return
     */
    public Example createExampl(Category category) {
        Example example = new Example(Category.class);
        Example.Criteria criteria = example.createCriteria();
        if (category != null) {
            //id
            if (!StringUtils.isEmpty(category.getId())) {
                criteria.andEqualTo(category.getId());
            }
            //分类名称
            if (!StringUtils.isEmpty(category.getName())) {
                criteria.andLike("name", "%" + category.getName() + "%");
            }
            //商品数量
            if (!StringUtils.isEmpty(category.getGoodsNum())) {
                criteria.andEqualTo("goodsNum", category.getGoodsNum());
            }
            //是否显示
            if (!StringUtils.isEmpty(category.getIsShow())) {
                criteria.andEqualTo("idShow", category.getIsShow());
            }
            //是否导航
            if (!StringUtils.isEmpty(category.getIsMenu())) {
                criteria.andEqualTo("isMenu", category.getIsMenu());
            }
            //排序
            if (!StringUtils.isEmpty(category.getSeq())) {
                criteria.andEqualTo("seq", category.getSeq());
            }
            //父级id
            if (!StringUtils.isEmpty(category.getParentId())) {
                criteria.andEqualTo("parentId", category.getParentId());
            }
            //模板id
            if (!StringUtils.isEmpty(category.getTemplateId())) {
                criteria.andEqualTo("templateId", category.getTemplateId());
            }
        }
        return example;
    }
}
