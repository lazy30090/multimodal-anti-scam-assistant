package com.juntong.multimodalantiscamassistant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.juntong.multimodalantiscamassistant.module.*.mapper")
public class MultimodalAntiScamAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultimodalAntiScamAssistantApplication.class, args);
    }

}
