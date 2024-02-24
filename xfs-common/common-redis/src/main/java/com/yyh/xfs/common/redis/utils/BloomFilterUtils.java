package com.yyh.xfs.common.redis.utils;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author yyh
 * @date 2024-02-24
 */
@Configuration
public class BloomFilterUtils {

    private final RedissonClient redissonClient;

    public BloomFilterUtils(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 初始化布隆过滤器
     * @param bloomFilterName 布隆过滤器名称
     * @param expectedInsertions 预期插入数量
     * @param fpp 误差率
     */
    public void initBloomFilter(String bloomFilterName, long expectedInsertions, double fpp) {
        redissonClient.getBloomFilter(bloomFilterName).tryInit(expectedInsertions, fpp);
    }

    /**
     * 添加值
     * @param bloomFilterName 布隆过滤器名称
     * @param value 值
     */
    public void addBloomFilter(String bloomFilterName, String value) {
        redissonClient.getBloomFilter(bloomFilterName).add(value);
    }

    /**
     * 批量添加值
     * @param bloomFilterName 布隆过滤器名称
     * @param values 值
     */
    public void addAllBloomFilter(String bloomFilterName, List<String> values) {
        values.forEach(value -> redissonClient.getBloomFilter(bloomFilterName).add(value));
    }

    /**
     * 判断是否存在
     * @param bloomFilterName 布隆过滤器名称
     * @param value 值
     * @return 是否存在
     */
    public boolean mightContainBloomFilter(String bloomFilterName, String value) {
        return redissonClient.getBloomFilter(bloomFilterName).contains(value);
    }

    /**
     * 删除布隆过滤器
     * @param bloomFilterName 布隆过滤器名称
     */
    public void deleteBloomFilter(String bloomFilterName) {
        redissonClient.getBloomFilter(bloomFilterName).delete();
    }

    /**
     * 获取布隆过滤器的预期插入数量
     * @param bloomFilterName 布隆过滤器名称
     * @return 预期插入数量
     */
    public long getExpectedInsertionsBloomFilter(String bloomFilterName) {
        if (!redissonClient.getBloomFilter(bloomFilterName).isExists()) {
            return 0;
        }
        return redissonClient.getBloomFilter(bloomFilterName).getExpectedInsertions();
    }

    /**
     * 获取布隆过滤器的误差率
     * @param bloomFilterName 布隆过滤器名称
     * @return 误差率
     */
    public double getFppBloomFilter(String bloomFilterName) {
        return redissonClient.getBloomFilter(bloomFilterName).getFalseProbability();
    }

    /**
     * 获取布隆过滤器的实际插入数量
     * @param bloomFilterName 布隆过滤器名称
     * @return 实际插入数量
     */
    public long getBloomFilterSize(String bloomFilterName) {
        if (!redissonClient.getBloomFilter(bloomFilterName).isExists()) {
            return 0;
        }
        return redissonClient.getBloomFilter(bloomFilterName).getSize();
    }

}
