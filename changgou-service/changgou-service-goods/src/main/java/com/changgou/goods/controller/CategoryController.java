package com.changgou.goods.controller;

import com.changgou.goods.pojo.Category;
import com.changgou.goods.service.CategoryService;
import com.github.pagehelper.PageInfo;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父id查询商品分类
     * @param parentId
     * @return
     */
    @GetMapping("/list/{parentId}")
    public Result findListByParentId(@PathVariable(value = "parentId") Integer parentId) {
        List<Category> list = categoryService.findListByParentId(parentId);
        return new Result(true, StatusCode.OK, "查询商品分类", list);
    }


    @PostMapping("/search/{page}/{size}")
    public Result findPage(@PathVariable(value = "page") Integer page,
                           @PathVariable(value = "size") Integer size,
                           @RequestBody Category category) {
        PageInfo<Category> pageInfo = categoryService.findPage(category, page, size);
        return new Result(true, StatusCode.OK, "条件分页查询成功", pageInfo);
    }

    @GetMapping("/search/{page}/{size}")
    public Result findPage(@PathVariable(value = "page") Integer page,
                           @PathVariable(value = "size") Integer size) {
        PageInfo<Category> pageInfo = categoryService.fingPage(page, size);
        return new Result(true, StatusCode.OK, "分页查询成功", pageInfo);
    }

    @PostMapping("/search")
    public Result findList(@RequestBody Category category) {
        List<Category> list = categoryService.findByCategory(category);
        return new Result(true, StatusCode.OK, "条件查询成功", list);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable(value = "id") Integer id) {
        categoryService.deleteById(id);
        return new Result(true, StatusCode.OK, "删除成功");
    }


    @PutMapping("/{id}")
    public Result update(@PathVariable(value = "id") Integer id,
                         @RequestBody Category category) {
        category.setId(id);
        categoryService.update(category);
        return new Result(true, StatusCode.OK, "修改成功");
    }

    @PostMapping
    public Result add(@RequestBody Category category) {
        categoryService.add(category);
        return new Result(true, StatusCode.OK, "新增一个成功");
    }

    @GetMapping("/{id}")
    public Result<Category> findById(@PathVariable(value = "id") Integer id) {
        Category category = categoryService.findById(id);
        return new Result(true, StatusCode.OK, "查询一个成功", category);
    }

    @GetMapping
    public Result findAll() {
        List<Category> categorys = categoryService.findAll();
        return new Result(true, StatusCode.OK, "查询全部成功", categorys);
    }
}
