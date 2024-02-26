package com.yyh.xfs.notes.consumer;

import com.yyh.xfs.common.constant.RocketMQConsumerGroupConstant;
import com.yyh.xfs.common.constant.RocketMQTopicConstant;
import com.yyh.xfs.common.redis.utils.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yyh
 * @date 2024-02-24
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMQTopicConstant.NOTES_REMOVE_REDIS_TOPIC,
        consumerGroup = RocketMQConsumerGroupConstant.NOTES_REMOVE_REDIS_CONSUMER_GROUP)
public class RemoveCacheConsumer implements RocketMQListener<String> {

    private final RedisCache redisCache;

    public RemoveCacheConsumer(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    @Override
    public void onMessage(String s) {
        log.info("remove cache: {}", s);
        redisCache.delAllPrefix(s);
    }
}
