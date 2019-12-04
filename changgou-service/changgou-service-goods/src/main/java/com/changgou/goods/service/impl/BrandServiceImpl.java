package com.changgou.goods.service.impl;

import com.changgou.goods.dao.BrandMapper;
import com.changgou.goods.pojo.Brand;
import com.changgou.goods.service.BrandService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {
    @Autowired(required = false)
    private BrandMapper brandMapper;

    @Override
    public List<Brand> findAll() {
        return brandMapper.selectAll();
    }

    @Override
    public Brand findById(Integer id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    @Override
    public void add(Brand brand) {
        brandMapper.insert(brand);
    }

    @Override
    public void deleteById(Integer id) {
        brandMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void update(Brand brand) {
        brandMapper.updateByPrimaryKeySelective(brand);
        //Selective  会判断传入的pojo属性是否为空。效果：可以只更新pojo的某一个属性，其他属性信息还在

    }

    //条件查询
    @Override
    public List<Brand> findList(Brand brand) {
        // 封装查询条件
        Example example = createExample(brand);
        return brandMapper.selectByExample(example);
    }

    //分页查询
    @Override
    public PageInfo<Brand> findPage(Integer pageNum, Integer size) {
        //设置分页条件
        PageHelper.startPage(pageNum, size);
        List<Brand> list = brandMapper.selectAll();
        return new PageInfo<Brand>(list);
    }

    //按照条件分页查询
    @Override
    public PageInfo<Brand> findPageByBrand(Integer pageNum, Integer size, Brand brand) {
        //设置查询条件
        Example example = createExample(brand);
        //设置分页条件
        PageHelper.startPage(pageNum, size);
        List<Brand> list = brandMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    /**
     * 根据cateId查询品牌列表
     *
     * @param cateId
     * @return
     */
    @Override
    public List<Brand> findListByCateId(Integer cateId) {
        List<Brand> list = brandMapper.findListByCateId(cateId);
        return list;
    }


    /**
     * 构建查询条件;   条件通过pojo实体来传递，然后经过处理 返回不同条件的查询功能
     */
    private Example createExample(Brand brand) {
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();
        if (brand != null) {
            //根据名称模糊查询
            if (!StringUtils.isEmpty(brand.getName())) {    //isEmpty 返回null或者空串
                criteria.andLike("name", "%" + brand.getName() + "%");
            }
            //根据品牌首字母查询
            if (!StringUtils.isEmpty(brand.getLetter())) {
                criteria.andEqualTo("letter", brand.getLetter());
            }
            //其他条件也可以扩展
        }
        return example;   //返回的是   查询条件。
    }
}
