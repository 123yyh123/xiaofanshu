package com.yyh.xfs.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author yyh
 * @date 2023-12-09
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.yyh.xfs.auth", "com.yyh.xfs.user", "com.yyh.xfs.common"})
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
