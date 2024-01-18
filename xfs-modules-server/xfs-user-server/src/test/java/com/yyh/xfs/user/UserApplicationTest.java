package com.yyh.xfs.user;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yyh
 * @date 2024-01-18
 */
@SpringBootTest
public class UserApplicationTest {
    @Test
    void test3() {
        List<String> list = new ArrayList<>();
        list.add("你好");
        list.add("大家好");
        list.add("我们好");
        String jsonString = JSON.toJSONString(list);
        System.out.println(jsonString);
    }
}
