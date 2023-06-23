package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.controller.CommonController;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    @Override
    public void saveSetmealWithDish(SetmealDto setmealDto) {
        //先保存简单数据
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        List<SetmealDish> dishes = setmealDishes.stream().peek((item) -> {
            item.setSetmealId(setmealDto.getId());
        }).collect(Collectors.toList());
        //存储
        setmealDishService.saveBatch(dishes);
        //吧传出过来的图片转存
        CommonController.saveFile(setmealDto.getImage());
    }

    @Override
    public SetmealDto getSetmealWithDishById(Long id) {
        SetmealDto setmealDto = new SetmealDto();
        //现获取基本数据
        Setmeal setmealbyId = this.getById(id);
        BeanUtils.copyProperties(setmealbyId,setmealDto);

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SetmealDish::getSetmealId,id);

        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }

    @Override
    public void updateSetmealWithDish(SetmealDto setmealDto) {
        //先更新基本数据
        this.updateById(setmealDto);

        //先删除setmeal_dish中的数据，然后再添加进去，因为这涉及多数据，增删改，不能直接改
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmealDto.getId() != null,SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        //删除后再添加
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().peek((item)->{
            item.setSetmealId(setmealDto.getId());
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public void deleteSetmealWithDish(Long[] ids) {
        List<Long> listids = Arrays.asList(ids);
        //先判断是否能删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,listids).eq(Setmeal::getStatus,1);

        int count = this.count(queryWrapper);
        if(count>0){
            throw new CustomException("套餐正在售卖中，不能删除");
        }
        this.removeByIds(listids);
        //删除关联表
        LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getSetmealId,listids);
        setmealDishService.remove(queryWrapper1);
    }
}
