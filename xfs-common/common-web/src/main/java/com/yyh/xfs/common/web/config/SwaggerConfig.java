package com.yyh.xfs.common.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * @author yyh
 * @date 2023-09-17
 */
@Configuration
public class SwaggerConfig {
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.yyh.xfs.*.controller"))
                .paths(PathSelectors.any())
                .build();

    }
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .description("小番薯接口文档")
                .contact(new Contact("杨亚豪", "", "yahaoyang929@vip.qq.com"))
                .version("v1.0")
                .title("接口文档")
                .build();
    }
}
