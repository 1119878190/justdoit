package com.justodit.controller.advice;

import com.justodit.util.JustDoItUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

//异常处理
@ControllerAdvice(annotations = Controller.class)//只扫描带有Controller注解的类
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class})
    public void handlerException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常"+e.getMessage());
        for (StackTraceElement element :e.getStackTrace()){
            logger.error(element.toString());
        }

        //判断请求的方式
        String xRequestedWith = request.getHeader("x-requested-with");//固定写法
        if ("XMLHttpRequest".equals(xRequestedWith)){
            //是异步请求
            response.setContentType("application/plain;charset=utf-8");  //返回普通字符串,在页面上手动转为json $.parseJSON

            PrintWriter writer = response.getWriter();
            writer.write(JustDoItUtil.getJsonString(1,"服务器异常"));

        }else {
            //不是异步请求,重定向到500页面
            response.sendRedirect(request.getContextPath()+"/error");
        }
    }

}
