package com.changgou.goods.controller;

import com.changgou.goods.pojo.Spec;
import com.changgou.goods.service.SpecService;
import com.github.pagehelper.PageInfo;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/spec")
public class SpecController {
    @Autowired
    private SpecService specService;

    /**
     * 根据cateId查询规格信息
     * @param cateId
     * @return
     */
    @GetMapping("/list/{cateId}")
    public Result list(@PathVariable(value = "cateId") Integer cateId) {
        List<Spec> list = specService.findListByCateId(cateId);
        return new Result(true, StatusCode.OK, "根据cateId查询规格信息成功", list);
    }

    @PostMapping("/search/{page}/{size}")
    public Result findPage(@PathVariable(value = "page") Integer page,
                           @PathVariable(value = "size") Integer size,
                           @RequestBody Spec spec) {
        PageInfo<Spec> pageInfo = specService.findPage(spec, page, size);
        return new Result(true, StatusCode.OK, "条件分页查询成功", pageInfo);
    }

    @GetMapping("/search/{page}/{size}")
    public Result findPage(@PathVariable(value = "page") Integer page,
                           @PathVariable(value = "size") Integer size) {
        PageInfo<Spec> pageInfo = specService.findPage(page, size);
        return new Result(true, StatusCode.OK, "分页查询成功", pageInfo);
    }

    @PostMapping("/search")
    public Result findList(@RequestBody Spec spec) {
        List<Spec> list = specService.findList(spec);
        return new Result(true, StatusCode.OK, "条件查询成功", list);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable(value = "id") Integer id) {
        specService.delete(id);
        return new Result(true, StatusCode.OK, "删除成功");
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable(value = "id") Integer id,
                         @RequestBody Spec spec) {
        spec.setId(id);
        specService.update(spec);
        return new Result(true, StatusCode.OK, "修改成功");
    }

    @PostMapping
    public Result add(@RequestBody Spec spec) {
        specService.add(spec);
        return new Result(true, StatusCode.OK, "新增一个成功");
    }

    @GetMapping("/{id}")
    public Result findById(@PathVariable(value = "id") Integer id) {
        Spec spec = specService.findById(id);
        return new Result(true, StatusCode.OK, "查询一个成功", spec);
    }

    @GetMapping
    public Result findAll() {
        List<Spec> specs = specService.findAll();
        return new Result(true, StatusCode.OK, "查询全部成功", specs);
    }
}
