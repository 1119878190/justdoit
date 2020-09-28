package com.justodit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class DemoApplication {

    @PostConstruct
    public void init(){
        //解决netty启动冲突的问题
        //Netty4utils  setAvailableProcessors()
        System.setProperty("es.set.netty.runtime.available.processors","false");
    }


    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
