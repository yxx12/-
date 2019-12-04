package com.changgou.goods.controller;

import com.changgou.goods.pojo.Para;
import com.changgou.goods.service.ParaService;
import com.github.pagehelper.PageInfo;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/para")
public class ParaController {
    @Autowired
    private ParaService paraService;

    /**
     * 根据cateId查询参数信息成功
     * @param cateId
     * @return
     */
    @GetMapping("/list/{cateId}")
    public Result list(@PathVariable(value = "cateId")Integer cateId){
       List<Para> list= paraService.findListByCateId(cateId);
        return new Result(true,StatusCode.OK,"根据cateId查询参数信息成功",list);
    }
    @PostMapping("/search/{page}/{size}")
    public Result findPage(@PathVariable(value = "page") Integer page,
                           @PathVariable(value = "size") Integer size,
                           @RequestBody Para para) {
        PageInfo<Para> pageInfo = paraService.findPage(para, page, size);
        return new Result(true, StatusCode.OK, "条件分页查询成功", pageInfo);
    }

    @GetMapping("/search/{page}/{size}")
    public Result findPage(@PathVariable(value = "page") Integer page,
                           @PathVariable(value = "size") Integer size) {
        PageInfo<Para> pageInfo = paraService.findPage(page, size);
        return new Result(true, StatusCode.OK, "分页查询成功", pageInfo);
    }

    @PostMapping("/search")
    public Result findList(@RequestBody Para para) {
        List<Para> list = paraService.findList(para);
        return new Result(true, StatusCode.OK, "条件查询成功", list);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable(value = "id") Integer id) {
        paraService.delete(id);
        return new Result(true, StatusCode.OK, "删除成功");
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable(value = "id") Integer id,
                         @RequestBody Para para) {
        para.setId(id);
        paraService.update(para);
        return new Result(true, StatusCode.OK, "修改成功");
    }

    @PostMapping
    public Result add(@RequestBody Para para) {
        paraService.add(para);
        return new Result(true, StatusCode.OK, "新增一个成功");
    }

    @GetMapping("/{id}")
    public Result findById(@PathVariable(value = "id") Integer id) {
        Para para = paraService.findById(id);
        return new Result(true, StatusCode.OK, "查询一个成功", para);
    }

    @GetMapping
    public Result findAll() {
        List<Para> paras = paraService.findAll();
        return new Result(true, StatusCode.OK, "查询全部成功", paras);
    }
}
