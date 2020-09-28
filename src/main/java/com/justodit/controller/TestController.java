package com.justodit.controller;


import com.justodit.util.JustDoItUtil;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;

@Controller
public class TestController {

    //ajax示例
    @PostMapping("/ajax")
    @ResponseBody
    public String testAjax(String name,int age){
        System.out.println(name);
        System.out.println(age);
        return JustDoItUtil.getJsonString(0,"操作成功");
    }
}
