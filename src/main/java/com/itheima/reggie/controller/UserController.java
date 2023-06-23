package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送手机短信验证码
     * */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();

        if (StringUtils.isNotEmpty(phone)){
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code = {}",code);
            //调用阿里云短信服务，发送

//            SMSUtils.sendMessage("瑞吉外卖","SMS_279311838",phone,code);
//            SMSUtils.sendMessage("阿里云短信测试","SMS_154950909",phone,code);
            //需要存储生成的验证码
            session.setAttribute(phone,code);
            return R.success("手机验证码发送成功");
        }
        return R.error("短信发送失败");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String,String> map, HttpSession session){

        //从map中获取手机号和验证码
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();

        //从session中获取验证码，比对，如果比对成功
        Object codeInSession = session.getAttribute(phone);
        //如果能成功，说明登录成功
        if(codeInSession != null && codeInSession.equals(code)){
        //如果为新用户，则自动完成注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if(user ==null ){
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return  R.success(user);
        }

        return R.error("登录失败");
    }

    @PostMapping("/loginout")
    public R<String> loginOut(HttpSession session){
        session.removeAttribute("user");

        return R.success("登出成功");
    }
}
