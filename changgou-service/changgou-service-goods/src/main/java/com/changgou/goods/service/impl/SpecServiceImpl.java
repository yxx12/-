package com.changgou.goods.service.impl;

import com.changgou.goods.dao.CategoryMapper;
import com.changgou.goods.dao.SpecMapper;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.pojo.Spec;
import com.changgou.goods.service.SpecService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class SpecServiceImpl implements SpecService {
    @Autowired(required = false)
    private SpecMapper specMapper;

    @Autowired(required = false)
    private CategoryMapper categoryMapper;

    @Override
    public List<Spec> findAll() {
        return specMapper.selectAll();
    }

    /**
     * 根据cateId查询规格信息
     *
     * @param cateId
     * @return
     */
    @Override
    public List<Spec> findListByCateId(Integer cateId) {
        Category category = categoryMapper.selectByPrimaryKey(cateId);
        Spec spec =new Spec();
        spec.setTemplateId(category.getTemplateId());
        List<Spec> list = specMapper.select(spec);
        return list;
    }

    @Override
    public Spec findById(Integer id) {
        return specMapper.selectByPrimaryKey(id);
    }

    @Override
    public void add(Spec spec) {
        specMapper.insertSelective(spec);
    }

    @Override
    public void update(Spec spec) {
        specMapper.updateByPrimaryKeySelective(spec);
    }

    @Override
    public void delete(Integer id) {
        specMapper.deleteByPrimaryKey(id);
    }

    @Override
    public PageInfo<Spec> findPage(Spec spec, int page, int size) {
        //分页
        PageHelper.startPage(page, size);
        //搜索条件构建
        Example example = createExample(spec);
        //执行搜索
        return new PageInfo<Spec>(specMapper.selectByExample(example));
    }

    @Override
    public PageInfo<Spec> findPage(int page, int size) {
        //静态分页
        PageHelper.startPage(page, size);
        //分页查询
        return new PageInfo<Spec>(specMapper.selectAll());
    }


    @Override
    public List<Spec> findList(Spec spec) {
        Example example = createExample(spec);
        return specMapper.selectByExample(example);
    }


    public Example createExample(Spec spec) {
        Example example = new Example(Spec.class);
        Example.Criteria criteria = example.createCriteria();

        if (spec != null) {
            //id查询
            if (!StringUtils.isEmpty(spec.getId())) {
                criteria.andEqualTo(spec.getId());
            }
            //模板名称
            if (!StringUtils.isEmpty(spec.getName())) {
                criteria.andLike("name", "%" + spec.getName() + "%");
            }
            // 规格选项
            if (!StringUtils.isEmpty(spec.getOptions())) {
                criteria.andEqualTo("options", spec.getOptions());
            }
            // 排序
            if (!StringUtils.isEmpty(spec.getSeq())) {
                criteria.andEqualTo("seq", spec.getSeq());
            }
            // 模板ID
            if (!StringUtils.isEmpty(spec.getTemplateId())) {
                criteria.andEqualTo("templateId", spec.getTemplateId());
            }
        }
        return example;
    }
}
