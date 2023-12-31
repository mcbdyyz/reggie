package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional  //事务注解
public interface DishService extends IService<Dish> {
    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);
    //更新菜品信息和口味信息
    void updateWithFlavor(DishDto dishDto);

    boolean deleteByIdWithFlavor(Long[] ids);
}
