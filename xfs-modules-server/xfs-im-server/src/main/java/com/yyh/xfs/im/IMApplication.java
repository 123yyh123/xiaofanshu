package com.yyh.xfs.im;

import com.yyh.xfs.im.server.IMServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author yyh
 * @date 2023-12-24
 */
@SpringBootApplication
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
