package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){

        //设置用户id
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        //查询当前菜品或者套餐是否存在套餐中，存在加一
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
        if (shoppingCart.getDishId() != null){
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else {
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        ShoppingCart shoppingCartServiceOne = shoppingCartService.getOne(queryWrapper);
        //如果已经存在，就在原来基础上加一
        if(shoppingCartServiceOne != null){
            Integer number = shoppingCartServiceOne.getNumber();
            shoppingCartServiceOne.setNumber(number+1);
            shoppingCartService.updateById(shoppingCartServiceOne);
        }else {
            //如果不存在，则添加到购物车，输入默认是1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            shoppingCartServiceOne = shoppingCart;
        }
        return R.success(shoppingCartServiceOne);
    }

    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        log.info(shoppingCart.toString());
        //通过菜品id和用户id，来减少购物单，判断如果数量为1，则删除这个菜品
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        //先判断这个是套餐还是菜品
        if(shoppingCart.getDishId()!=null){
            //这个是菜品
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else {
            //这个是套餐id
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //通过取到的值判断这是不是最后一个，如果是，则删除
        ShoppingCart one = shoppingCartService.getOne(queryWrapper);
        if(one ==null){
            throw new CustomException("信息获取错误，请清除套餐再试");
        }
        Integer number = one.getNumber();
        if(number >1){
            one.setNumber(number-1);
            shoppingCartService.updateById(one);
        }else {
            //这个是最后一个
            shoppingCartService.remove(queryWrapper);
            one.setNumber(0);
        }

        return R.success(one);
    }


    /**
     * 显示购物车内容
     * */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        //查看购物车
        log.info("查看购物车....");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        //Asc是升序，desc是降序
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 清空购物车
     * */
    @DeleteMapping("/clean")
    public R<String> clean(){

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        shoppingCartService.remove(queryWrapper);

        return  R.success("清空购物车成功");
    }
}
