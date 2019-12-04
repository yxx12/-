package com.changgou.goods.dao;

import com.changgou.goods.pojo.Brand;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand> {
    @Select("SELECT tb.id,tb.name FROM tb_category_brand tcb JOIN tb_brand tb ON tcb.brand_id= tb.id AND tcb.category_id=#{cateId}")
    List<Brand> findListByCateId(Integer cateId);
}
