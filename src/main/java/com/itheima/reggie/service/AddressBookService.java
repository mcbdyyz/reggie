package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.entity.User;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface AddressBookService extends IService<AddressBook> {

}
