package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    static LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
    static LambdaQueryWrapper<Setmeal> setmealQueryWrapper = new LambdaQueryWrapper<>();

    @Override
    public void remove(Long id,Integer type) {
        if (type == 1){
            dishQueryWrapper.eq(null != id,Dish::getCategoryId,id);
            int dishCount = dishService.count(dishQueryWrapper);
            //查询当前分类是否关联了菜品，
            if(dishCount >0 ){
                //如果已经关联，抛出一个业务异常
                throw new CustomException("当前分类项关联了菜品，不能删除！");
            }
        }else if (type == 2){
            setmealQueryWrapper.eq(null != id , Setmeal::getCategoryId,id);
            int setMealCount = setmealService.count(setmealQueryWrapper);
            if(setMealCount > 0 ){
                //如果已经关联，抛出一个业务异常
                throw new CustomException("当前分类项关联了套餐，不能删除！");
            }
        }
        super.removeById(id);
    }
    @Override
    public void remove(Long id) {
            dishQueryWrapper.eq(null != id,Dish::getCategoryId,id);
            int dishCount = dishService.count(dishQueryWrapper);
            //查询当前分类是否关联了菜品，
            if(dishCount >0 ){
                //如果已经关联，抛出一个业务异常
                throw new CustomException("当前分类项关联了菜品，不能删除！");
            }
            setmealQueryWrapper.eq(null != id , Setmeal::getCategoryId,id);
            int setMealCount = setmealService.count(setmealQueryWrapper);
            if(setMealCount > 0 ){
                //如果已经关联，抛出一个业务异常
                throw new CustomException("当前分类项关联了套餐，不能删除！");
            }
        super.removeById(id);
    }
}
