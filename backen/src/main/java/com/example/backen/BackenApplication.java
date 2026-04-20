package com.example.backen;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.backen.mapper")
public class BackenApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackenApplication.class, args);
    }

}
