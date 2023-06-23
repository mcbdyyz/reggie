package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器
 * */

@ControllerAdvice(annotations = {RestController.class,Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandles {

    //异常处理，
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());

        if (ex.getMessage().contains("Duplicate entry")){
            String[] split = ex.getMessage().split(" ");
            String msg = split[2]+"已存在";
            return R.error(msg);
        }
        return R.error("未知错误");
    }

    @ExceptionHandler(CustomException.class)
    public R<String> CategoryException(CustomException ex){
        log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public void FileException(IOException ex){
        log.error(ex.getMessage());

//        if(ex.getMessage().contains("D:\\software\\reggieImage")){
//            return R.error("图片信息获取失败");
//        }
//        return  R.error("未知错误，请联系管理员");
    }

}
