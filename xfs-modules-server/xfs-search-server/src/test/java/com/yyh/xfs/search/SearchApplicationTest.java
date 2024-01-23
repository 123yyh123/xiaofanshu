package com.yyh.xfs.search;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

/**
 * @author yyh
 * @date 2024-01-24
 */
@SpringBootTest
public class SearchApplicationTest {
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Test
    void test1() {
        System.out.println(elasticsearchRestTemplate);
    }
}
