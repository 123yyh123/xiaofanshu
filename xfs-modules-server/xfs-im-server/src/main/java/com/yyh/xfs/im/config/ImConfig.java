package com.yyh.xfs.im.config;

import com.yyh.xfs.common.redis.constant.BloomFilterMap;
import com.yyh.xfs.common.redis.utils.BloomFilterUtils;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author yyh
 * @date 2024-02-25
 */
@Configuration
public class ImConfig {
    private final BloomFilterUtils bloomFilterUtils;

    public ImConfig(BloomFilterUtils bloomFilterUtils) {
        this.bloomFilterUtils = bloomFilterUtils;
    }

    /**
     * 初始化布隆过滤器
     */
    @PostConstruct
    public void initBloomFilter() {
        // 先判断有没有该布隆过滤器，没有则初始化
        long expectedInsertionsBloomFilter = bloomFilterUtils.getExpectedInsertionsBloomFilter(BloomFilterMap.NOTES_ID_BLOOM_FILTER);
        if (expectedInsertionsBloomFilter > 0) {
            return;
        }
        bloomFilterUtils.initBloomFilter(BloomFilterMap.NOTES_ID_BLOOM_FILTER, 100000, 0.01);
    }
}
