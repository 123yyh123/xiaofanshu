package com.yyh.xfs.user.config;

import com.yyh.xfs.common.redis.constant.BloomFilterMap;
import com.yyh.xfs.common.redis.utils.BloomFilterUtils;
import com.yyh.xfs.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yyh
 * @date 2024-02-24
 */
@Configuration
public class UserConfig {
    private final UserMapper userMapper;
    private final BloomFilterUtils bloomFilterUtils;

    public UserConfig(UserMapper userMapper, BloomFilterUtils bloomFilterUtils) {
        this.userMapper = userMapper;
        this.bloomFilterUtils = bloomFilterUtils;
    }

    /**
     * 初始化布隆过滤器
     */
    @PostConstruct
    public void initBloomFilter() {
        List<String> list = userMapper.selectList(null).stream().map(userDO -> String.valueOf(userDO.getId())).collect(Collectors.toList());
        // 先判断有没有该布隆过滤器，没有则初始化
        long bloomFilterSize = bloomFilterUtils.getBloomFilterSize(BloomFilterMap.USER_ID_BLOOM_FILTER);
        if(bloomFilterSize==list.size()){
            return;
        }
        bloomFilterUtils.initBloomFilter(BloomFilterMap.USER_ID_BLOOM_FILTER, list.isEmpty() ? 10000 : list.size() * 4L, 0.01);
        bloomFilterUtils.addAllBloomFilter(BloomFilterMap.USER_ID_BLOOM_FILTER, list);
    }
}
