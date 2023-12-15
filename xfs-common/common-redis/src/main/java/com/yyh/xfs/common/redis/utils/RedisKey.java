package com.yyh.xfs.common.redis.utils;

/**
 * @author yyh
 * @date 2023-12-15
 */
public class RedisKey {
    /**
     * 构建redis key
     * @param prefix 前缀
     * @param key key
     * @return redis key
     */
    public static String build(String prefix, String key) {
        return prefix + key;
    }
}
