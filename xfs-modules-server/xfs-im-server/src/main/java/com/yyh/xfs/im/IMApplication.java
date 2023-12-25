package com.yyh.xfs.im;

import com.yyh.xfs.im.server.IMServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * @author yyh
 * @date 2023-12-24
 */
@SpringBootApplication
public class IMApplication implements CommandLineRunner {
    @Autowired
    private IMServer imServer;
    public static void main(String[] args) {
        SpringApplication.run(IMApplication.class, args);
    }

    @Override
    public void run(String... args) {
        imServer.start();
    }
}
