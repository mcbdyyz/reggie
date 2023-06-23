package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1.将页面提交的密码password进行md5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2.根据页面提交的用户名查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        //3.如果没有查询到则返回登录失败结果
        if(emp ==null){
            return R.error("登录失败");
        }
        //4.密码对比，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return R.error("用户名或密码错误");
        }
        //5.查看员工登录状态，如果已经为禁用，则返回员工已经禁用结果
        if (emp.getStatus() == 0){
            return R.error("账号已禁用");
        }
        //6.登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理Session中保存的当前登录的员工id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功.");
    }

    /**
     *添加员工
     * */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工{}",employee.toString());
        //设置初始密码123456
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
        //从session中取创建人
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);
        employeeService.save(employee);
        return R.success("新增员工成功");
    }
    /**
     * 员工信息分页查询
     * */
    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize,String name){
        //1.构造分页构造器
        Page pageInfo = new Page(page,pageSize);
        //2.条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<Employee>();
        //添加过滤条件
        //TODO StringUtils注意不要导错包，common包的
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //4.排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //5.执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     * */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee){
//        log.info(employee.toString());
        //设置更新时间
//        employee.setUpdateTime(LocalDateTime.now());
        //设置修改用户
//        employee.setUpdateUser((long)request.getSession().getAttribute("employee"));
        //这样就可以给里面的数据进行修改了
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    /**
     * 根据id查询员工信息
     * */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable long id){
        Employee employee = employeeService.getById(id);
        if (employee != null){
        return R.success(employee);

        }
        return R.error("查询失败");
    }
}
