package com.changgou.goods.dao;

import com.changgou.goods.pojo.Sku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:shenkunlin
 * @Description:Sku的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface SkuMapper extends Mapper<Sku> {
    /**
     * 更新库存，防止超卖
     * @param id
     * @param num
     * @return
     */
    @Update("update tb_sku set num=num-#{num} where id=#{id} and num>#{num}")
    int decrCount(@Param("id") Long id, @Param("num") Integer num);
}
