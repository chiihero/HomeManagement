package com.chii.homemanagement;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.chii.homemanagement.mapper")
@EnableScheduling
//war使用
@ServletComponentScan
public class HomeManagementApplication  extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(HomeManagementApplication.class, args);
    }
}
