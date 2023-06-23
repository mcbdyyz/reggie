package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    /**
     * 新增分类
     * */
    @PostMapping
    public R<String> addCategory(@RequestBody Category category){
        categoryService.save(category);

        return R.success("新增分类成功");
    }

    /**
     * 分页查询
     * */
    @GetMapping("/page")
    public R<Page> page(Integer page,Integer pageSize){
        Page<Category> pageInfo = new Page<Category>(page,pageSize);

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.orderByDesc(Category::getSort);

        categoryService.page(pageInfo,queryWrapper);
        return  R.success(pageInfo);
    }
    /**
     * 根据id删除分类
     * */
    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("该传出来的id：{}",ids);
        categoryService.remove(ids);
        return R.success("删除成功");
    }

    /**
     * 通过id修改分类信息
     * */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }
    /**
     * 新增菜品 中 提取菜品分类列表
     * */
    @GetMapping("/list")
    public R<List<Category>> categoryList(Category category){
        //因为有排序要求，所以这里的接收参数是类
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper();
        //新增查询规则
        queryWrapper.eq(null != category.getType(),Category::getType,category.getType());
        //新增排序规则
        queryWrapper.orderByDesc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //进行列表查询
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
