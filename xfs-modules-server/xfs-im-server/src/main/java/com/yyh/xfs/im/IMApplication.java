package com.yyh.xfs.im;

import com.yyh.xfs.common.web.config.FeignConfig;
import com.yyh.xfs.im.server.IMServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author yyh
 * @date 2023-12-24
 * @desc im启动类
 * 排除FeignConfig.class，因为websocket无法获取header，所以需要在参数中传递，不需要FeignConfig.class
 */
@SpringBootApplication(exclude = {FeignConfig.class})
@EnableFeignClients
public class IMApplication implements CommandLineRunner {
    @Autowired
    private IMServer imServer;
    public static void main(String[] args) {
        SpringApplication.run(IMApplication.class, args);
    }
    /**
     * 项目启动后执行
     */
    @Override
    public void run(String... args) {
        // 启动netty
        imServer.start();
    }
}
