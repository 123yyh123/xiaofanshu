package com.yyh.xfs.im.consumer;

import com.alibaba.fastjson.JSON;
import com.yyh.xfs.common.redis.constant.BloomFilterMap;
import com.yyh.xfs.common.redis.utils.BloomFilterUtils;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.im.handler.types.ChatHandler;
import com.yyh.xfs.im.vo.MessageVO;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.yyh.xfs.im.handler.IMServerHandler.USER_CHANNEL_MAP;

/**
 * @author yyh
 * @date 2024-02-21
 */
@Component
@Slf4j
@RocketMQMessageListener(
        topic = "notes-praiseAndCollect-remind-topic",
        consumerGroup = "praise-and-collect-notification-consumer-group")
public class PraiseAndCollectNotificationConsumer implements RocketMQListener<MessageExt> {
    private final ChatHandler chatHandler;
    private final BloomFilterUtils bloomFilterUtils;

    public PraiseAndCollectNotificationConsumer(ChatHandler chatHandler,
                                                BloomFilterUtils bloomFilterUtils) {
        this.chatHandler = chatHandler;
        this.bloomFilterUtils = bloomFilterUtils;
    }
    @Override
    public void onMessage(MessageExt messageExt) {
        if (bloomFilterUtils.mightContainBloomFilter(BloomFilterMap.ROCKETMQ_IDEMPOTENT_BLOOM_FILTER, messageExt.getMsgId())) {
            log.info("消息已经消费过：{}", messageExt.getMsgId());
            return;
        }
        String s = new String(messageExt.getBody());
        log.info("收到消息：{}", s);
        MessageVO message = JSON.parseObject(s, MessageVO.class);
        Channel channel = USER_CHANNEL_MAP.get(message.getTo());
        chatHandler.sendMessage(channel, message);
        bloomFilterUtils.addBloomFilter(BloomFilterMap.ROCKETMQ_IDEMPOTENT_BLOOM_FILTER, messageExt.getMsgId());
    }
}
