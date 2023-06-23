package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishService dishService;

    /**
     * 添加数据
     * */
    @PostMapping
    public R<String> save( @RequestBody SetmealDto setmealDto){

        setmealService.saveSetmealWithDish(setmealDto);

        return R.success("添加成功");
    }

    /**
     * 分页查询
     * */
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(Integer page,Integer pageSize,String name){
        Page<Setmeal> setmealPage = new Page<>(page,pageSize);
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like(StringUtils.isNotEmpty(name),Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(setmealPage,queryWrapper);

        //对查出来的信息进行处理成SetmealDto的page，再返回出去
        Page<SetmealDto> setmealDtoPage = new Page<>();
        //复制setmealPage的值 并且排除records中的值，因为这个值和Dto的类不同，会复制失败
        BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");

        List<Setmeal> records = setmealPage.getRecords();
        List<Long> categoryList = records.stream().map((Setmeal::getCategoryId)).collect(Collectors.toList());
        if(categoryList.size()>0){
            LambdaQueryWrapper<Category> queryWrapper1 =new LambdaQueryWrapper();
            queryWrapper1.select(Category::getId,Category::getName).in(Category::getId,categoryList);
            List<Category> list = categoryService.list(queryWrapper1);
            Map<Long, String> categoryByIdMap = list.stream().collect(Collectors.toMap(Category::getId, Category::getName));

            //对数组类进行处理，转换成Dto的数组类
            List<SetmealDto> setmealDtos = records.stream().map((item) -> {
                SetmealDto setmealDto = new SetmealDto();
                BeanUtils.copyProperties(item,setmealDto);
                setmealDto.setCategoryName(categoryByIdMap.get(item.getCategoryId()));
                return setmealDto;
            }).collect(Collectors.toList());
            setmealDtoPage.setRecords(setmealDtos);

            return R.success(setmealDtoPage);
        }
        return R.error("获取为空");
    }

    /**
     * 根据id获取套餐信息
     * */
    @GetMapping("/{id}")
    public R<SetmealDto> getSetmealByid(@PathVariable Long id){
        SetmealDto setmealWithDishById = setmealService.getSetmealWithDishById(id);
        return R.success(setmealWithDishById);
    }

    /**
     * 修改套餐
     * */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateSetmealWithDish(setmealDto);
        return R.success("套餐修改成功");
    }

    /**
     * 删除套餐
     * */
    @DeleteMapping
    public R<String> delete(Long[] ids){

        setmealService.deleteSetmealWithDish(ids);
        return R.success("删除成功");
    }

    /**
     * 停售套餐
     * */
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable Integer status, Long[] ids){
        List<Setmeal> collect = Arrays.stream(ids).map((item) -> {
            Setmeal setmeal = new Setmeal();
            setmeal.setStatus(status);
            setmeal.setId(item);
            return setmeal;
        }).collect(Collectors.toList());

        setmealService.updateBatchById(collect);
        return R.success("操作成功");
    }
    /**
     * 根据条件查询套餐数据
     * */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);

    }
    /**
     * 查询套餐数据,这个有问题前端制作不全
     * */
    @GetMapping("/dish/{id}")
    public R<List<Dish>> dishById(@PathVariable Long id){

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(id != null,SetmealDish::getSetmealId,id);

        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        List<Long> dishList = new ArrayList<>();
        for (SetmealDish setmealDish : list) {
            Long dishId = setmealDish.getDishId();
            dishList.add(dishId);
        }

        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(dishList.size()>0,Dish::getId,dishList);
        List<Dish> dishListById = dishService.list(dishLambdaQueryWrapper);
        return R.success(dishListById);
    }
//    @GetMapping("/dish/{id}")
//    public R<List<Dish>> dishById(@PathVariable Long id){
//        return R.error("前端还未完善套餐信息查询");
//    }
}
