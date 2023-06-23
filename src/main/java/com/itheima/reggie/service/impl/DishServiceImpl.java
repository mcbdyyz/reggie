package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.controller.CommonController;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * 新增菜品,因为这个涉及到了多张表的操作，所以要加入事务控制
     * */
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //先保存基本信息
        this.save(dishDto);

        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        //前段已经进行了非空判断，所以这里可有可无，但为了程序健全，还是写着吧
        if (flavors != null){
            flavors = flavors.stream().peek((item)->{
                item.setDishId(dishId);
            }).collect(Collectors.toList());
            //保存菜品口味数据 dishFlavor
            dishFlavorService.saveBatch(flavors);
            //最后吧图片移除缓存，放入久存
            CommonController.saveFile(dishDto.getImage());

        }
    }

    /**
     * 根据id查询菜品和对应的口味信息
     * */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        DishDto dishDto = new DishDto();
        //查询菜品基本信息，从dish表
        Dish byId = this.getById(id);
        BeanUtils.copyProperties(byId,dishDto);

        //查询当前菜品对应的口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(list);
        return dishDto;
    }

    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息
        this.updateById(dishDto);
        //清理当前菜品对应口味数据，
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dishDto.getId() != null,DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //添加当前提交过来的口味数据
        List<DishFlavor> flavors = dishDto.getFlavors();
        //处理集合
        flavors.stream().peek((item)-> item.setDishId(dishDto.getId())).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据id删除多个表的数据
     * */
    @Override
    public boolean deleteByIdWithFlavor(Long[] ids) {
        List<Long> listId = Arrays.asList(ids);
        boolean b = this.removeByIds(listId);//删除当前表的数据

        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DishFlavor::getDishId,listId);
        //根据条件删除对应口味表的数据
        dishFlavorService.remove(queryWrapper);
        return b;
    }

}
