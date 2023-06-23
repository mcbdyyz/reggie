package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否登录
 *
 * */

@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    //路径匹配，支持通配符
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        Long empId = (Long) request.getSession().getAttribute("employee");
        Long userId= (Long) request.getSession().getAttribute("user");
        //1.获取本次请求的URI
        String requestURI = request.getRequestURI();
         String[] urls = new String[]{
                "/employee/login",  //登录请求直接放行
                "/employee/logout", //登出请求直接放行
                "/backend/**",       //静态资源的网页都是死数据，可以被访问，要控制的是动态资源才拦截
                 "/front/**",
                 "/common/**",
                 "/user/sendMsg",
                 "/user/login",
         };
        //2.判断本次请求是否需要处理
        boolean check = check(urls, requestURI);
        //3.如果不需要处理，则直接放行
        if (check){
            filterChain.doFilter(request,response);
            return;
        }
        //4.判断登录状态，如果已经登录，则直接放行
        if(empId !=null){

            BaseContext.setCurrentId(empId);
            filterChain.doFilter(request,response);
            return;
        }
        if( userId!=null){
            log.info("用户已登录，用户id为：{}",userId);
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request,response);
            return;
        }
        //5.如果未登录则返回未登录结果
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
//        log.info("拦截到请求：{}",request.getRequestURL());
//        filterChain.doFilter(request,response);
    }
    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }
}
