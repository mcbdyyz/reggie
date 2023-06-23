package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.User;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface UserService extends IService<User> {

}
