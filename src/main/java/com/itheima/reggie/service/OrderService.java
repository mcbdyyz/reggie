package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.User;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface OrderService extends IService<Orders> {

    void submit(Orders orders);
}
