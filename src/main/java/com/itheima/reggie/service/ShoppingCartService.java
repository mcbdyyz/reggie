package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.entity.User;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ShoppingCartService extends IService<ShoppingCart> {

}
