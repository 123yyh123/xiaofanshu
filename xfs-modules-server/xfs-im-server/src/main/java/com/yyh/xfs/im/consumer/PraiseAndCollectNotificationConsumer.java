package com.yyh.xfs.im.consumer;

import com.alibaba.fastjson.JSON;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.im.handler.types.ChatHandler;
import com.yyh.xfs.im.vo.MessageVO;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
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
public class PraiseAndCollectNotificationConsumer implements RocketMQListener<String> {
    private final ChatHandler chatHandler;

    public PraiseAndCollectNotificationConsumer(ChatHandler chatHandler) {
        this.chatHandler = chatHandler;
    }
    @Override
    public void onMessage(String s) {
        log.info("收到消息：{}", s);
        MessageVO message = JSON.parseObject(s, MessageVO.class);
        Channel channel = USER_CHANNEL_MAP.get(message.getTo());
        chatHandler.sendMessage(channel, message);
    }
}
