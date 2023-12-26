package com.yyh.xfs.im;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * @author yyh
 * @date 2023-12-25
 */
@SpringBootTest
public class IMApplicationTest {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Test
    void test1() {
        System.out.println("IMApplicationTest");
    }

    @Test
    void test2() {
        System.out.println(Runtime.getRuntime().availableProcessors());
    }
}
