package com.changgou.goods.controller;

import com.changgou.goods.pojo.Template;
import com.changgou.goods.service.TemplateService;
import com.github.pagehelper.PageInfo;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/template")
public class TemplateController {
    @Autowired
    private TemplateService templateService;

    /**
     * 查询模板名称
     * @param cateId
     * @return
     */
    @GetMapping("/find/{cateId}")
    public Result findTempByCateId(@PathVariable(value = "cateId") Integer cateId) {
        Template template = templateService.findTempByCateId(cateId);
        return new Result(true, StatusCode.OK, "查询模板名称成功", template);
    }

    @PostMapping("/search/{page}/{size}")
    public Result findPage(@PathVariable(value = "page") Integer page,
                           @PathVariable(value = "size") Integer size,
                           @RequestBody Template template) {
        PageInfo<Template> pageInfo = templateService.findPage(template, page, size);
        return new Result(true, StatusCode.OK, "条件分页查询成功", pageInfo);
    }

    @GetMapping("/search/{page}/{size}")
    public Result findPage(@PathVariable(value = "page") Integer page,
                           @PathVariable(value = "size") Integer size) {
        PageInfo<Template> pageInfo = templateService.findPage(page, size);
        return new Result(true, StatusCode.OK, "分页查询成功", pageInfo);
    }

    @PostMapping("/search")
    public Result findList(@RequestBody Template template) {
        List<Template> list = templateService.findList(template);
        return new Result(true, StatusCode.OK, "条件查询成功", list);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable(value = "id") Integer id) {
        templateService.delete(id);
        return new Result(true, StatusCode.OK, "删除成功");
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable(value = "id") Integer id,
                         @RequestBody Template template) {
        template.setId(id);
        templateService.update(template);
        return new Result(true, StatusCode.OK, "修改成功");
    }

    @PostMapping
    public Result add(@RequestBody Template template) {
        templateService.add(template);
        return new Result(true, StatusCode.OK, "新增一个成功");
    }

    @GetMapping("/{id}")
    public Result findById(@PathVariable(value = "id") Integer id) {
        Template template = templateService.findById(id);
        return new Result(true, StatusCode.OK, "查询一个成功", template);
    }

    @GetMapping
    public Result findAll() {
        List<Template> templates = templateService.findAll();
        return new Result(true, StatusCode.OK, "查询全部成功", templates);
    }
}
