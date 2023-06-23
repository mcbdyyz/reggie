package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrderController {

    @Autowired
    public OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        orderService.submit(orders);
        return R.success("下单成功");
    }
    @GetMapping("/userPage")
    public R<Page<Orders>> userPage(Integer page, Integer pageSize){
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        //根据时间来排序
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo);

        return  R.success(pageInfo);
//        return R.error("订单模块有bug暂时关闭");
    }


    @GetMapping("/page")
    public R<Page<Orders>> page (Integer page,Integer pageSize,String number,String beginTime,String endTime){
        log.info(beginTime);
        log.info(endTime);
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        //根据时间来排序
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Orders::getOrderTime);
        queryWrapper.like(number != null,Orders::getNumber,number);
        queryWrapper.between(beginTime!=null&&endTime!=null,Orders::getOrderTime,beginTime,endTime);
        orderService.page(pageInfo,queryWrapper);
        return  R.success(pageInfo);
    }
}
