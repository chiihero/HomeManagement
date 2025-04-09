package com.chii.homemanagement;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * 家庭物品管理系统应用入口
 * 
 * @author chii
 * @since 1.0.0
 */
@SpringBootApplication
@MapperScan("com.chii.homemanagement.mapper")
@EnableScheduling
//war使用
@ServletComponentScan
@OpenAPIDefinition(
    info = @Info(
        title = "家庭物品管理系统 API",
        version = "1.0.0",
        description = "家庭物品管理系统接口文档",
        contact = @Contact(name = "Chii", email = "admin@example.com"),
        license = @License(name = "MIT", url = "https://opensource.org/licenses/MIT")
    ),
    servers = {
        @Server(url = "/", description = "生产环境服务器"),
        @Server(url = "http://localhost:8080", description = "本地开发服务器")
    }
)
public class HomeManagementApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(HomeManagementApplication.class, args);
    }
}
