package com.chii.homemanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

/**
 * 转发头配置
 * 用于处理代理转发请求中的头信息，确保应用能正确识别客户端请求的原始地址和端口
 */
@Configuration
public class ForwardedHeaderConfig {

    /**
     * 创建 ForwardedHeaderFilter 实例
     * 此过滤器将处理 X-Forwarded-Host, X-Forwarded-Port, X-Forwarded-Proto 等HTTP头，
     * 使Spring MVC能正确生成重定向URL和绝对URLs
     *
     * @return ForwardedHeaderFilter 实例
     */
    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
} 