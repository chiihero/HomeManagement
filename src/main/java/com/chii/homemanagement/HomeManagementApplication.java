package com.chii.homemanagement;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.chii.homemanagement.mapper")
@EnableScheduling
public class HomeManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(HomeManagementApplication.class, args);
    }
}
