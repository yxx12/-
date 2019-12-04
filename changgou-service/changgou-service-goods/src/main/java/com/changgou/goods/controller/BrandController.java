package com.changgou.goods.controller;

import com.changgou.goods.pojo.Brand;
import com.changgou.goods.service.BrandService;
import com.github.pagehelper.PageInfo;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 根据cateId查询品牌列表
     * @param cateId
     * @return
     */
    @GetMapping("/list/{cateId}")
    public Result list(@PathVariable(value = "cateId") Integer cateId) {
        List<Brand> list = brandService.findListByCateId(cateId);
        return new Result<>(true, StatusCode.OK, "根据cateId查询品牌列表成功", list);
    }

    @GetMapping
    public Result<List<Brand>> findALl() {
        List<Brand> list = brandService.findAll();
        return new Result<>(true, StatusCode.OK, "查询成功", list);
    }

    @GetMapping("/{id}")
    public Result<Brand> findById(@PathVariable(value = "id") Integer id) {
        Brand brand = brandService.findById(id);
        return new Result<>(true, StatusCode.OK, "查询成功", brand);
    }

    @PostMapping
    public Result add(@RequestBody Brand brand) {
        brandService.add(brand);
        return new Result(true, StatusCode.OK, "添加成功");
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable(value = "id") Integer id) {
        brandService.deleteById(id);
        return new Result(true, StatusCode.OK, "删除成功");
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable(value = "id") Integer id, @RequestBody Brand brand) {

        brand.setId(id);
        brandService.update(brand);
        return new Result(true, StatusCode.OK, "更新成功");
    }

    @PostMapping("/search")
    public Result<List<Brand>> findList(@RequestBody Brand brand) {
        List<Brand> list = brandService.findList(brand);
        return new Result<>(true, StatusCode.OK, "条件查询成功", list);
    }

    @GetMapping("/search/{pageNum}/{size}")
    public Result<List<Brand>> findPage(@PathVariable(value = "pageNum") Integer pageNum,
                                        @PathVariable(value = "size") Integer size) {
        PageInfo<Brand> page = brandService.findPage(pageNum, size);
        return new Result<>(true, StatusCode.OK, "分页查询成功", page);
    }

    @PostMapping("/search/{pageNum}/{size}")
    public Result<List<Brand>> findPageByBrand(@PathVariable(value = "pageNum") Integer pageNum,
                                               @PathVariable(value = "size") Integer size,
                                               @RequestBody Brand brand) {
        PageInfo<Brand> page = brandService.findPageByBrand(pageNum, size, brand);
        return new Result<>(true, StatusCode.OK, "条件分页查询成功", page);
    }

}
