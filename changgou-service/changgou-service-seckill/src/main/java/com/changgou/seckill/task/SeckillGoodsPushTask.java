package com.changgou.seckill.task;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import entity.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillGoodsPushTask {
    @Autowired(required = false)
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Scheduled(cron = "0/59 0/1 * * * ?")
    public void seckillGoodsWtireRedis() {
        //获取当前时间，获取压入redis的商品列表
        List<Date> dateMenus = DateUtil.getDateMenus();
        //遍历时间菜单的时间点（菜单中的所有数据是偶数的整点时刻）
        for (Date dateMenu : dateMenus) {
            //时间菜单转化成string，作为存储redis的key
            String dataKey = DateUtil.data2str(dateMenu, DateUtil.PATTERN_YYYYMMDDHH);
            //构建秒杀条件 example
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("status", "1");      //上架状态
            criteria.andGreaterThanOrEqualTo("startTime", dateMenu);//大于秒杀开始时间
            criteria.andLessThan("endTime", DateUtil.addDateHour(dateMenu, 2));//小于结束时间
            criteria.andGreaterThan("stockCount", 0);    //库存大于0
            //去除重复的商品,
            Set keys = redisTemplate.boundHashOps("SeckillGoods_" + dataKey).keys();
            if (keys != null && keys.size() > 0) {
                criteria.andNotIn("id", keys);
            }
            //查询数据库，得到符合秒杀条件的商品
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
            //遍历商品列表并存储到rediss
            if (seckillGoodsList != null && seckillGoodsList.size() > 0) {
                for (SeckillGoods seckillGoods : seckillGoodsList) {
                    //将商品压入redis
                    redisTemplate.boundHashOps("SeckillGoods_" + dataKey).put(seckillGoods.getId(), seckillGoods);
                    //获取商品库存，new一个库存长度数组
                    Long[] idArrays = pushId(seckillGoods.getStockCount(), seckillGoods.getId());
                    //redis中建立一个队列，队列内容就是 库存长度的数组
                    redisTemplate.boundListOps("SeckillGoodsCountList_"+seckillGoods.getId()).leftPushAll(idArrays);
                    // 将当前商品对应的库存存储到redis中
                    redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillGoods.getId(),seckillGoods.getStockCount());
                }
            }
        }
    }

    /**
     * 创建一个秒杀商品库存长度的数组
     *
     * @param stockCount
     * @param id
     * @return
     */
    private Long[] pushId(Integer stockCount, Long id) {
        Long[] ids = new Long[stockCount];
        for (Integer i = 0; i < stockCount; i++) {
            ids[i] = id;
        }
        return ids;
    }
}
