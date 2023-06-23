package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

/**
 * 文件上传和下载
 *
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {
    private static String BASE_PATH;
    private static String BASE_PATH_CACHE;

    @Value("${reggie.path}")
    public void setBasePath(String basePath){
        BASE_PATH = basePath;
    }
    @Value("${reggie.pathCache}")
    public void setBasePathCache(String basePathCache){
        BASE_PATH_CACHE = basePathCache;
    }
    /**
     * 文件上传
     *
     * */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        log.info(file.toString());

        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        String fileName = UUID.randomUUID() + suffix;

        //判断目录是否存在
        File dir = new File(BASE_PATH_CACHE);
        if(!dir.exists()){
            //不存在，创建
            dir.mkdirs();
        }
        try {

            file.transferTo(new File(BASE_PATH_CACHE + fileName));
        }catch (IOException e){
            e.printStackTrace();
        }
        return R.success(fileName);
    }

    /**
     * 文件下载
     * */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws IOException {

        File file = new File(BASE_PATH + name);
        //判断文件是否存在
        if (!file.exists()){
            //不存在，则到缓存文件夹下找
            file = new File(BASE_PATH_CACHE +name);
        }
        //输入流
        FileInputStream fileInputStream = new FileInputStream(file);
        //输出流
        ServletOutputStream outputStream = response.getOutputStream();
        //设置响应类型
        response.setContentType("image/jpeg"); //图片类型
        //先通过输入流读
        int len = 0;
        byte[] bytes = new byte[1024];
        while ((len = fileInputStream.read(bytes)) != -1){
            //bytes 读到的数据， 从0 开始写， 写len个，len通过read定义了长度，通过response写数据，不需要返回值，会自己写回浏览器
            outputStream.write(bytes,0,len);
            //刷新流
            outputStream.flush();
        }
        //关闭流
        outputStream.close();
        fileInputStream.close();
    }

    public static void saveFile(String name){
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(BASE_PATH_CACHE + name));
            FileOutputStream outputStream = new FileOutputStream(new File(BASE_PATH+name));
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                //bytes 读到的数据， 从0 开始写， 写len个，len通过read定义了长度
                outputStream.write(bytes, 0, len);
                //刷新流
                outputStream.flush();
            }

            //关闭流
            outputStream.close();
            fileInputStream.close();
        } catch (IOException e){
            e.printStackTrace();
        }

    }
}
