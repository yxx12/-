package com.changgou.item.controller;

import com.changgou.item.service.PageService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/page")
public class PageController {

    @Autowired(required = false)
    private PageService pageService;

    @GetMapping("/createHtml/{id}")
    public Result createHtml(@PathVariable(value = "id") Long id) {
        pageService.createHtml(id);
        return new Result(true, StatusCode.OK, "创建sku页面成功");
    }


}
