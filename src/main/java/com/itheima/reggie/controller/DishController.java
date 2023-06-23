package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增 菜品
     * */
    @PostMapping
    public R<String> add(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 分页查询（旧版） 问题：在循环里进行sql查询，导致效率变低
     * */
    @GetMapping("/page2")
    public R<Page> page2( Integer page,Integer pageSize,String name){
        long l1 = System.currentTimeMillis();
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name),Dish::getName,name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo,queryWrapper);
        //对象拷贝 忽略records
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        //取出pageInfo的records
        List<Dish> records = pageInfo.getRecords();

//        List<Long> cateId = records.stream().map(Dish::getCategoryId).collect(Collectors.toList());
//        queryWrapper1.select(Category::getId,Category::getName).in(cateId.size() >0,Category::getId,cateId);

        //处理records
        List<DishDto> collect = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId(); //分类id
        LambdaQueryWrapper<Category> queryWrapper1 = new LambdaQueryWrapper();
        queryWrapper1.select(Category::getId,Category::getName).eq(Category::getId,categoryId);


            //根据id查询分类对象
            Category byId = categoryService.getOne(queryWrapper1);
            String name1 = byId.getName();
            dishDto.setCategoryName(name1);
            return dishDto;

        }).collect(Collectors.toList());
        dishDtoPage.setRecords(collect);
        System.out.println(dishDtoPage);
        long l2 = System.currentTimeMillis();

        System.out.println("总共耗时："+(l2-l1));
        return  R.success(dishDtoPage);
    }

    /**
     * 分页查询，直接查询想要的数据，然后转换为诶map，再放入循环进行赋值效率比旧版提升约50%
     * */
    @GetMapping("/page")
    public R<Page> page( Integer page,Integer pageSize,String name){
        long l1 = System.currentTimeMillis();
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //设置分页条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //分页模糊查询条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Dish::getName,name);
        //分页排序查询条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行分页查询
        dishService.page(pageInfo,queryWrapper);
        //对象拷贝 忽略records
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        //取出pageInfo的records
        List<Dish> records = pageInfo.getRecords();

        //取出records中的Dish中的id存为list作为条件准备
        List<Long> cateId = records.stream().map(Dish::getCategoryId).collect(Collectors.toList());
        if (cateId.size()>0){
            LambdaQueryWrapper<Category> queryWrapper1 = new LambdaQueryWrapper();
            //选择id和name字段，通过cateId查Category对应的数据出来
            queryWrapper1.select(Category::getId,Category::getName).in(cateId.size() >0,Category::getId,cateId);
            List<Category> list = categoryService.list(queryWrapper1);
            //把查询的值通过stream流转换为map数组
            Map<Long, String> collect1 = list.stream().collect(Collectors.toMap(Category::getId, Category::getName));


            List<DishDto> collect = records.stream().map((item) -> { //        处理records
                DishDto dishDto = new DishDto(); //创建DishDto对象来接收值
                BeanUtils.copyProperties(item,dishDto); //吧除了id以外的值先复制到dishDto类中
                dishDto.setCategoryName(collect1.get(item.getCategoryId()));  //通过item中的id来对应map中的id键，找到对应的值，然后赋值到dishDto中
                return dishDto;
                //转换为list
            }).collect(Collectors.toList());
            dishDtoPage.setRecords(collect);
            //设置page中的数据

            //计算耗时
            long l2 = System.currentTimeMillis();
            System.out.println("总共耗时："+(l2-l1));
            //返回
            return  R.success(dishDtoPage);
        }
        return  R.success(dishDtoPage);
    }

    /**
     * 根据id回显要修改的数据
     * */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }
    /**
     * 修改菜品
     * */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }
    /**
     * 根据id进行删除
     * */
    @DeleteMapping
    public R<String> delete(Long[] ids){


        boolean b = dishService.deleteByIdWithFlavor(ids);
        if(b){
            return R.success("删除成功");
        }
        return R.error("删除失败，未知错误");
    }
    /**
     * 根据id对数据库进行修改
     * */
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable Integer status,Long ...ids){
        //处理数据
        List<Dish> collect = Arrays.stream(ids).map((item) -> {
            Dish dish = new Dish();
            dish.setId(item);
            dish.setStatus(status);
            return dish;
        }).collect(Collectors.toList());

        boolean update = dishService.updateBatchById(collect);
        if (update){
            return R.success("操作成功");
        }
        return  R.error("操作失败");
    }

    /**
     * 查询套餐中菜品集合
     * **/
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        //构造条件查询
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(Dish::getCategoryId,dish.getCategoryId());
        //查询为启售的
        queryWrapper.eq(Dish::getStatus,1);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //查询
        List<Dish> list = dishService.list(queryWrapper);
        //获得对应菜品id的集合来查询
        List<Long> dishIdList = list.stream().map(Dish::getId).collect(Collectors.toList());
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.in(dishIdList.size() > 0,DishFlavor::getDishId,dishIdList);
        //多个菜品的集合
        List<DishFlavor> dishList = dishFlavorService.list(lambdaQueryWrapper);

//        for (DishFlavor list1 : dishList) {
//
//            flavorList.add(list1.getName());
//            flavorList.add(list1.getValue());
//            dishFlavorMap.put(list1.getDishId(),flavorList);
//        }
        List<DishDto> collect = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            List<DishFlavor> dishFlavorsList = new ArrayList<>();
            for (DishFlavor dishFlavor : dishList) {
                if (item.getId().equals(dishFlavor.getDishId())) {
                    dishFlavorsList.add(dishFlavor);
                }
            }

            dishDto.setFlavors(dishFlavorsList);
            return dishDto;
        }).collect(Collectors.toList());
        System.out.println(collect);
        return R.success(collect);
    }

}
