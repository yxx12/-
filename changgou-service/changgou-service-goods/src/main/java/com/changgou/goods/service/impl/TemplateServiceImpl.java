package com.changgou.goods.service.impl;

import com.changgou.goods.dao.CategoryMapper;
import com.changgou.goods.dao.TemplateMapper;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.pojo.Template;
import com.changgou.goods.service.TemplateService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class TemplateServiceImpl implements TemplateService {
    @Autowired(required = false)
    private TemplateMapper templateMapper;
    @Autowired(required = false)
    private CategoryMapper categoryMapper;

    @Override
    public List<Template> findAll() {
        return templateMapper.selectAll();
    }

    @Override
    public Template findTempByCateId(Integer cateId) {
        Category category = categoryMapper.selectByPrimaryKey(cateId);
        Template template = templateMapper.selectByPrimaryKey(category.getTemplateId());
        return template;
    }

    @Override
    public Template findById(Integer id) {
        return templateMapper.selectByPrimaryKey(id);
    }

    @Override
    public void add(Template template) {
        templateMapper.insertSelective(template);
    }

    @Override
    public void update(Template template) {
        templateMapper.updateByPrimaryKeySelective(template);
    }

    @Override
    public void delete(Integer id) {
        templateMapper.deleteByPrimaryKey(id);
    }

    @Override
    public PageInfo<Template> findPage(Template template, int page, int size) {
        //分页
        PageHelper.startPage(page, size);
        //搜索条件构建
        Example example = createExample(template);
        //执行搜索
        return new PageInfo<Template>(templateMapper.selectByExample(example));
    }

    @Override
    public PageInfo<Template> findPage(int page, int size) {
        //静态分页
        PageHelper.startPage(page, size);
        //分页查询
        return new PageInfo<Template>(templateMapper.selectAll());
    }

    @Override
    public List<Template> findList(Template template) {
        Example example = createExample(template);
        return templateMapper.selectByExample(example);
    }


    public Example createExample(Template template) {
        Example example = new Example(Template.class);
        Example.Criteria criteria = example.createCriteria();

        if (template != null) {
            //id查询
            if (!StringUtils.isEmpty(template.getId())) {
                criteria.andEqualTo(template.getId());
            }
            //模板名称
            if (!StringUtils.isEmpty(template.getName())) {
                criteria.andLike("name", "%"+template.getName()+"%");
            }
            //规格数量
            if (!StringUtils.isEmpty(template.getSpecNum())) {
                criteria.andEqualTo("specNum", template.getSpecNum());
            }
            //参数数量
            if (!StringUtils.isEmpty(template.getParaNum())) {
                criteria.andEqualTo("paraNum", template.getParaNum());
            }
        }
        return example;
    }
}
