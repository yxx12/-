package com.changgou.search.service;

import java.util.Map;

public interface SkuInfoService {
    /**
     * 将正常状态的库存信息保存到ES索引库中
     * @return
     */
    void importSkuInfoToEs();

    /**
     * 检索功能
     * @param searchMap
     * @return
     */
    Map<String,Object> search(Map<String,String> searchMap);
}
