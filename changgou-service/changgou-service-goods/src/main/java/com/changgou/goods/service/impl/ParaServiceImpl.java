package com.changgou.goods.service.impl;

import com.changgou.goods.dao.CategoryMapper;
import com.changgou.goods.dao.ParaMapper;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.pojo.Para;
import com.changgou.goods.service.ParaService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class ParaServiceImpl implements ParaService {
    @Autowired(required = false)
    private ParaMapper paraMapper;
    @Autowired(required = false)
    private CategoryMapper categoryMapper;

    @Override
    public List<Para> findAll() {
        return paraMapper.selectAll();
    }

    /**
     * 根据cateId查询参数信息成功
     *
     * @param cateId
     * @return
     */
    @Override
    public List<Para> findListByCateId(Integer cateId) {
        Category category = categoryMapper.selectByPrimaryKey(cateId);
        Para para = new Para();
        para.setTemplateId(category.getTemplateId());
        return paraMapper.select(para);
    }

    @Override
    public Para findById(Integer id) {
        return paraMapper.selectByPrimaryKey(id);
    }

    @Override
    public void add(Para para) {
        paraMapper.insertSelective(para);
    }

    @Override
    public void update(Para para) {
        paraMapper.updateByPrimaryKeySelective(para);
    }

    @Override
    public void delete(Integer id) {
        paraMapper.deleteByPrimaryKey(id);
    }

    @Override
    public PageInfo<Para> findPage(Para para, int page, int size) {
        //分页
        PageHelper.startPage(page, size);
        //搜索条件构建
        Example example = createExample(para);
        //执行搜索
        return new PageInfo<Para>(paraMapper.selectByExample(example));
    }

    @Override
    public PageInfo<Para> findPage(int page, int size) {
        //静态分页
        PageHelper.startPage(page, size);
        //分页查询
        return new PageInfo<Para>(paraMapper.selectAll());
    }

    @Override
    public List<Para> findList(Para para) {
        Example example = createExample(para);
        return paraMapper.selectByExample(example);
    }


    public Example createExample(Para para) {
        Example example = new Example(Para.class);
        Example.Criteria criteria = example.createCriteria();

        if (para != null) {
            //id查询
            if (!StringUtils.isEmpty(para.getId())) {
                criteria.andEqualTo(para.getId());
            }
            //模板名称
            if (!StringUtils.isEmpty(para.getName())) {
                criteria.andLike("name", "%" + para.getName() + "%");
            }
            // 规格选项
            if (!StringUtils.isEmpty(para.getOptions())) {
                criteria.andEqualTo("options", para.getOptions());
            }
            // 排序
            if (!StringUtils.isEmpty(para.getSeq())) {
                criteria.andEqualTo("seq", para.getSeq());
            }
            // 模板ID
            if (!StringUtils.isEmpty(para.getTemplateId())) {
                criteria.andEqualTo("templateId", para.getTemplateId());
            }
        }
        return example;
    }
}
