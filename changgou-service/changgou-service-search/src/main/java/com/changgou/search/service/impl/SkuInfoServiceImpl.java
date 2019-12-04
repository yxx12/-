package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuInfoMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuInfoService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class SkuInfoServiceImpl implements SkuInfoService {
    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired(required = false)
    private SkuFeign skuFeign;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 将正常状态的库存信息保存到ES索引库中
     */
    @Override
    public void importSkuInfoToEs() {
        //通过feign调用微服务接口
        List<Sku> skuList = skuFeign.findSkusByStatus("1").getData();
        if (skuList.size() > 0 && skuList != null) {
            //将pojo集合转换成json字符串
            String jsonString = JSON.toJSONString(skuList);
            //将json字符串 反序列换成pojo 集合
            List<SkuInfo> skuInfos = JSON.parseArray(jsonString, SkuInfo.class);

            for (SkuInfo skuInfo : skuInfos) {
                String spec = skuInfo.getSpec();
                Map<String, Object> specMap = JSON.parseObject(spec, Map.class);
                //主要是为了 给 SpecMap字段设置值
                skuInfo.setSpecMap(specMap);
            }
            skuInfoMapper.saveAll(skuInfos);
        }
    }

    /**
     * 商品检索
     *
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        //构建检索条件(后期有多个条件检索，因此我们专门封装一个方法)
        NativeSearchQueryBuilder builder = builderBasicQuery(searchMap);

        //根据关键字查询
        Map<String, Object> resultMap = searchForPage(builder);

     //  //分类列表
     //  List<String> categoryList = searchCategoryList(builder);
     //  resultMap.put("categoryList", categoryList);
     //  //品牌列表
     //  List<String> brandList = searchBrandList(builder);
     //  resultMap.put("brandList", brandList);
     //  //规格列表
     //  Map<String, Set<String>> specList = searchSpecMap(builder);
     //  resultMap.put("specList", specList);
        //一次性过滤 分类、品牌、规格
       Map<String, Object> map = groupList(builder);

        resultMap.putAll(map);

        return resultMap;
    }

    /**
     * 分类列表
     * 品牌列表
     * 规格列表
     *
     * @param builder
     * @return
     */
    private Map<String, Object> groupList(NativeSearchQueryBuilder builder) {
        //构架查询条件（往builder里面添加查询条件）
        builder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName").size(1000));
        builder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName").size(1000));
        builder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword").size(1000));
        //查询到的结果数据
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        Aggregations aggregations = aggregatedPage.getAggregations();

        //获取到分组的结果集
        List<String> categoryList = getGroupList(aggregations, "skuCategory");
        List<String> brandList = getGroupList(aggregations, "skuBrand");
        List<String> specs = getGroupList(aggregations, "skuSpec");

        //将规格 的数据转换成map结构
        Map<String, Set<String>> specList = pullMap(specs);

        Map<String, Object> map = new HashMap<>();
        map.put("categoryList", categoryList);
        map.put("brandList", brandList);
        map.put("specList", specList);

        return map;
    }

    //获取结果集中的 各项结果集数据
    private List<String> getGroupList(Aggregations aggregations, String groupName) {
        //获取指定 分组的数据，
        StringTerms stringTerms = aggregations.get(groupName);
        //获取指定分组数据的 list结合形式
        List<StringTerms.Bucket> buckets = stringTerms.getBuckets();
        List<String> list = new ArrayList<>();
        //遍历出分组的所有数据，装进String 泛型 的list集合 返回
        for (StringTerms.Bucket bucket : buckets) {
            String keyAsString = bucket.getKeyAsString();
            list.add(keyAsString);
        }
        return list;
    }

    /**
     * 根据条件查询并封装商品数据
     *
     * @param builder
     * @return 商品结果集
     */
    private Map<String, Object> searchForPage(NativeSearchQueryBuilder builder) {

        HighlightBuilder.Field highligtField = new HighlightBuilder.Field("name"); //对字段中的关键字进行高亮
        highligtField.preTags("<font color='red'>");
        highligtField.postTags("</font>");
        builder.withHighlightFields(highligtField);

        SearchResultMapper searchResultMapper = new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                SearchHits hits = searchResponse.getHits();
                List<T> list = new ArrayList<>();
                long totalHits = hits.getTotalHits();
                for (SearchHit hit : hits) {
                    String source = hit.getSourceAsString();    //获取到普通的结果集，是个json串
                    SkuInfo skuInfo = JSON.parseObject(source, SkuInfo.class);

                    HighlightField highlightField = hit.getHighlightFields().get("name"); //获取到高亮的字段
                    if (!StringUtils.isEmpty(highlightField)) {
                        Text[] fragments = highlightField.getFragments();                     //获取到高亮结果集
                        skuInfo.setName(fragments[0].toString());
                    }

                    list.add((T) skuInfo);

                }
                return new AggregatedPageImpl<>(list, pageable, totalHits);
            }
        };

        //构建查询条件
        NativeSearchQuery build = builder.build();
        //查询商品，返回一个分页对象
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(build, SkuInfo.class, searchResultMapper);
        List<SkuInfo> skuInfos = page.getContent();     //结果集
        int totalPages = page.getTotalPages();          //总页数
        long totalElements = page.getTotalElements();   //总条数
        //将页面数据，封装进map 集合，并返回
        Map<String, Object> map = new HashMap<>();
        map.put("totalPages", totalPages);
        map.put("totalElements", totalElements);
        map.put("rows", skuInfos);
        map.put("pageNum", build.getPageable().getPageNumber() + 1);    // 当前页码
        map.put("pageSize", build.getPageable().getPageSize());         // 每页显示的条数
        return map;
    }


    /**
     * 统计规格列表
     *
     * @param builder
     * @return
     */
    private Map<String, Set<String>> searchSpecMap(NativeSearchQueryBuilder builder) {
        builder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword").size(10000));
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        //分组结果集
        Aggregations aggregations = page.getAggregations();
        StringTerms stringTerms = aggregations.get("skuSpec");
        List<StringTerms.Bucket> buckets = stringTerms.getBuckets();

        List<String> list = new ArrayList<>();
        for (StringTerms.Bucket bucket : buckets) {
            list.add(bucket.getKeyAsString());
        }
        //list的元素是一个个 规格的key和value；{"电视音响效果":"小影院" , "电视屏幕尺寸":"20英寸" , "尺码":"165"}
        //处理结果集,将list集合转换成map集合
        Map<String, Set<String>> map = pullMap(list);
        return map;
    }

    /**
     * 规格列表的结果集
     * 处理
     *
     * @param list
     * @return
     */
    private Map<String, Set<String>> pullMap(List<String> list) {
        Map<String, Set<String>> map = new HashMap<>();
        //处理规格数据（一个规格是一个元素，包括规格的key和value，存储在list集合中）
        for (String spec : list) {
            //将规格的数据转换成map形式
            Map specMap = JSON.parseObject(spec, Map.class);
            Set<Map.Entry<String, String>> entrySet = specMap.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                String key = entry.getKey();            //规格的名字  key
                String value = entry.getValue();        //规格的值  value

                Set<String> set = map.get(key);         //获取key对应的值
                //判断这个key在大map中是否存在
                if (set == null) {
                    //如果不存在就new 一个set集合用来存放 key对用的value
                    set = new HashSet();
                }
                set.add(value);
                map.put(key, set);
            }
        }
        return map;
    }

    /**
     * 统计品牌列表
     *
     * @param builder
     * @return
     */
    private List<String> searchBrandList(NativeSearchQueryBuilder builder) {
        builder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        //分组结果集
        Aggregations aggregations = page.getAggregations();
        StringTerms terms = aggregations.get("skuBrand");

        List<StringTerms.Bucket> buckets = terms.getBuckets();
        List<String> list = new ArrayList<>();
        for (StringTerms.Bucket bucket : buckets) {
            String keyAsString = bucket.getKeyAsString();
            list.add(keyAsString);
        }
        return list;
    }

    /**
     * 统计分类列表
     *
     * @param builder
     * @return
     */
    private List<String> searchCategoryList(NativeSearchQueryBuilder builder) {
        //下面这句啥意思，不懂
        builder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        //分组结果集
        Aggregations aggregations = page.getAggregations();
        StringTerms terms = aggregations.get("skuCategory");

        List<StringTerms.Bucket> buckets = terms.getBuckets();

        List<String> list = new ArrayList<>();
        for (StringTerms.Bucket bucket : buckets) {
            String keyAsString = bucket.getKeyAsString();
            list.add(keyAsString);
        }
        return list;
    }


    /**
     * 封装检索条件,
     * 返回 一个builder ,查询条件的构造器
     *
     * @param searchMap
     * @return
     */
    private NativeSearchQueryBuilder builderBasicQuery(Map<String, String> searchMap) {
        //封装检索条件
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        //封装过滤条件
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (searchMap != null) {
            // 1，根据关键字检索
            String keywords = searchMap.get("keywords");
            if (!StringUtils.isEmpty(keywords)) {
                builder.withQuery(QueryBuilders.matchQuery("name", keywords));
            }
            //-------------------------在商品列表的数据上，进行下面的过滤----------------------------------------------
            // 2，根据商品分类过滤
            String category = searchMap.get("category");
            if (!StringUtils.isEmpty(category)) {
                boolQuery.must(QueryBuilders.matchQuery("categoryName", category));
            }
            // 3，根据商品品牌检索
            String brand = searchMap.get("brand");
            if (!StringUtils.isEmpty(brand)) {
                boolQuery.must(QueryBuilders.matchQuery("brandName", brand));
            }
            // 4，根据规格过滤(不同规格的 key不一样，通过页面传过来的map集合的key，找到规格的key)
            Set<String> set = searchMap.keySet();
            for (String key : set) {
                if (key.startsWith("spec_")) {
                    boolQuery.must(QueryBuilders.matchQuery("specMap." + key.substring(5) + ".keyword", searchMap.get(key)));
                }
            }
            // 5，价格过滤,(一般页面 传递过来的是个字符串，中间用-分开)
            String price = searchMap.get("price");
            if (!StringUtils.isEmpty(price)) {
                String[] priceArray = price.split("-");
                //假如传递过来一个数据，肯定是查询比这个价格高的，一般默认最低的是0
                boolQuery.must(QueryBuilders.rangeQuery("price").gte(priceArray[0]));
                if (priceArray.length == 2) {
                    //既然传递过来两个数，后面的一个肯定是价格上线，所以查询比他价格低的
                    boolQuery.must(QueryBuilders.rangeQuery("price").lte(priceArray[1]));
                }
            }
            // 6, 结果排序
            String sortRule = searchMap.get("sortRule");        //排序规则
            String sortField = searchMap.get("sortField");      //排序的字段
            if (!StringUtils.isEmpty(sortField)) {
                builder.withSort(SortBuilders.fieldSort(sortField).order(SortOrder.valueOf(sortRule)));
            }
        }

        // 7, 结果分页（分页与请求参数 map集合是否为空无关紧要，他有自己的默认值）
        String pageNum = searchMap.get("pageNum");
        if (StringUtils.isEmpty(pageNum)) {
            pageNum = "1";
        }
        String pageSize = searchMap.get("pageSize");
        if (StringUtils.isEmpty(pageSize)) {
            pageSize = "10";
        }
        Integer size = Integer.parseInt(pageSize);                 //每页条数
        Integer page = Integer.parseInt(pageNum);                  //传入当前页面-1，就可以了，PageRequest会自动根据page，和size计算起始数据行
        Pageable pageable = PageRequest.of(page - 1, size);

        //添加过滤的条件
        builder.withFilter(boolQuery);
        //添加分页条件
        builder.withPageable(pageable);
        return builder;
    }
}
