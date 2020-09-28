package com.justodit.config;

//kaptcha验证码配置类
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {


    @Bean
    public Producer kaptchaProducer(){

        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width","100");//图片长度
        properties.setProperty("kaptcha.image.height","40");//图片高度
        properties.setProperty("kaptcha.textproducer.font.size","32");//字体大小
        properties.setProperty("kaptcha.textproducer.font.color","0,0,0");//RGB
        properties.setProperty("kaptcha.textproducer.char.string","0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");//随机字符的范围
        properties.setProperty("kaptcha.textproducer.char.length","4");//随机字符的长度
        properties.setProperty("kaptcha.noise.impl","com.google.code.kaptcha.impl.NoNoise");  //噪点类

        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return  kaptcha;


    }


}
