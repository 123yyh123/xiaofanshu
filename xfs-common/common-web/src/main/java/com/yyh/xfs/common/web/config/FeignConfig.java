package com.yyh.xfs.common.web.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yyh
 * @date 2023-09-26
 */
@Configuration
public class FeignConfig implements RequestInterceptor {
    private final HttpServletRequest request;

    public FeignConfig(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String token = request.getHeader("token");
        requestTemplate.header("token",token);
    }
}
