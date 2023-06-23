package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrderMapper;
import com.itheima.reggie.mapper.UserMapper;
import com.itheima.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Service
@Transactional
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    public ShoppingCartService shoppingCartService;
    @Autowired
    public UserService userService;
    @Autowired
    public AddressBookService addressBookService;
    @Autowired
    public OrderDetailService orderDetailService;
    @Override
    public void submit(Orders orders) {
        //获得当前用户id
        Long currentId = BaseContext.getCurrentId();

        //查询当前用户车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        if (list == null || list.size() == 0){
            throw new CustomException("购物车为空，不能下单");
        }

        //查询用户，地址数据
        User userId = userService.getById(currentId);
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if (addressBook == null){
            throw new CustomException("用户地址信息有误或为空，无法下单");
        }
        long orderId = System.currentTimeMillis();
        //向订单表插入数据，一条数据
        AtomicInteger amount = new AtomicInteger(0);

       List<OrderDetail> orderDetails = list.stream().map((item)->{
           OrderDetail orderDetail = new OrderDetail();
           orderDetail.setOrderId(orderId);
           orderDetail.setNumber(item.getNumber());
           orderDetail.setDishFlavor(item.getDishFlavor());
           orderDetail.setDishId(item.getDishId());
           orderDetail.setSetmealId(item.getSetmealId());
           orderDetail.setName(item.getName());
           orderDetail.setImage(item.getImage());
           orderDetail.setAmount(item.getAmount());
           amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
           return orderDetail;

       }).collect(Collectors.toList());

        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId.getId());
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(userId.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        //向订单明细表插入数据，多条数据
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }
}
