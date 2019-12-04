package com.changgou.search.controller;

import com.changgou.search.feign.SkuInfoFeign;
import com.changgou.search.pojo.SkuInfo;
import entity.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/search")
public class SkuController {
    @Autowired(required = false)
    private SkuInfoFeign skuInfoFeign;

    @GetMapping("/list")
    public String list(@RequestParam(required = false) Map<String, String> searchMap, Model model) {
        //处理特殊字符
        handlerSearchMap(searchMap);

        //搜索服务数据
        Map<String, Object> resultMap = skuInfoFeign.search(searchMap);
        //封装进 model(就相当于sission 域对象，共享数据的)
        model.addAttribute("resultMap", resultMap);

        //回显搜索条件
        model.addAttribute("searchMap", searchMap);
        // 组装url
        String url = getUrl(searchMap);
        model.addAttribute("url", url);
        //分页
        Page<SkuInfo> page = new Page<>(
                Long.parseLong(resultMap.get("totalElements").toString()),      //总条数
                Integer.parseInt(resultMap.get("pageNum").toString()),          //当前页码
                Integer.parseInt(resultMap.get("pageSize").toString())       //每页显示条数
        );
        model.addAttribute("page", page);
        //返回视图地址（框架已经默认约定 后缀名是  .html,静态页面的路径在所以无需再设置）
        return "search";


    }

    // 拼接url地址
    private String getUrl(Map<String, String> searchMap) {
        String url = "/search/list";
        if (searchMap != null && searchMap.size() > 0) {
            url += "?";
            Set<Map.Entry<String, String>> entrySet = searchMap.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                String key = entry.getKey();
                if (key.equals("pageNum")) {
                    continue;
                }
                url += key + "=" + entry.getValue() + "&";
            }
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
    /****
     * 替换特殊字符
     * @param searchMap
     */
    public void handlerSearchMap(Map<String,String> searchMap){
        if(searchMap!=null){
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                if(entry.getKey().startsWith("spec_")){
                    entry.setValue(entry.getValue().replace("+","%2B"));
                }
            }
        }
    }
}
