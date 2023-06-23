package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.mapper.AddressBookMapper;
import com.itheima.reggie.mapper.UserMapper;
import com.itheima.reggie.service.AddressBookService;
import com.itheima.reggie.service.UserService;
import org.springframework.stereotype.Service;


@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {


}
