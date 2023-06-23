package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface SetmealService extends IService<Setmeal> {
    void saveSetmealWithDish(SetmealDto setmealDto);

    SetmealDto getSetmealWithDishById(Long id);

    void updateSetmealWithDish(SetmealDto setmealDto);

    void deleteSetmealWithDish(Long[] ids);
}
