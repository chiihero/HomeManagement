package com.chii.homemanagement.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Web配置类
 */
@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;
    
    @Value("${file.base-url:/uploads}")
    private String baseUrl;
    
    /**
     * 配置静态资源处理器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的访问路径
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String uploadPathString = uploadPath.toString();
        
        // 确保以file:开头，适应不同操作系统
        if (!uploadPathString.startsWith("file:")) {
            uploadPathString = "file:" + uploadPathString.replace("\\", "/");
            if (!uploadPathString.endsWith("/")) {
                uploadPathString += "/";
            }
        }
        // 打印调试信息
        log.info("Upload Path: " + uploadPathString);
        log.info("Base URL: " + baseUrl);
        // 添加资源处理器，使上传的文件可以被访问
        registry.addResourceHandler(baseUrl + "/**")
                .addResourceLocations(uploadPathString);
        
        // 其他静态资源配置
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
    
    /**
     * 配置CORS跨域支持，支持从不同域名和端口访问
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept", 
                              "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers")
                .allowCredentials(true)
                .maxAge(3600);
    }
    
    /**
     * 创建CORS过滤器，确保所有请求都经过CORS处理
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedOriginPatterns(Arrays.asList("*"));
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setMaxAge(3600L);
        corsConfiguration.addExposedHeader("Authorization");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        
        return new CorsFilter(source);
    }
} 