package com.chii.homemanagement.config;//package com.chii.homemanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * springDoc-swagger标准配置
 *
 * @author huang cheng
 * 2021/8/13
 */
@Configuration
public class SpringDocSwaggerConfig {

    private static final String basePackage = "com.chii.homemanagement.controller";//需要扫描api路径
    private static final String headerName = "Authorization";//请求头名称


    @Bean
    public GroupedOpenApi usersGroup() {
        return GroupedOpenApi.builder()
                .group("users")
                .addOperationCustomizer((operation, handlerMethod) -> {
                    // 为每个操作添加安全要求
                    operation.addSecurityItem(new SecurityRequirement().addList(headerName));
                    return operation;
                })
                .packagesToScan(basePackage)
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        Components components = new Components();
        //添加右上角的统一安全认证
        components.addSecuritySchemes(headerName,
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("在下方输入JWT令牌")
        );

        return new OpenAPI()
                .components(components)
                .addSecurityItem(new SecurityRequirement().addList(headerName)) // 添加全局安全要求
                .info(apiInfo());
    }

    private Info apiInfo() {
        Contact contact = new Contact();
        contact.setEmail("853879993@qq.com");
        contact.setName("chii");
        return new Info()
                .title("chii-swagger文档")
                .version("1.0")
                .contact(contact)
                .license(new License().name("Apache 2.0").url("http://springdoc.org"));
    }

}