package com.yyh.xfs.im.consumer;

import com.alibaba.fastjson.JSON;
import com.yyh.xfs.common.redis.constant.BloomFilterMap;
import com.yyh.xfs.common.redis.utils.BloomFilterUtils;
import com.yyh.xfs.im.vo.MessageVO;
import com.yyh.xfs.im.handler.IMServerHandler;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yyh
 * @date 2023-12-25
 */
@Component
@Slf4j
@RocketMQMessageListener(
        topic = "find-user-topic",
        consumerGroup = "find-user-consumer-group",
        messageModel = MessageModel.BROADCASTING)
public class FindUserConsumer implements RocketMQListener<MessageExt>{

    private final BloomFilterUtils bloomFilterUtils;

    public FindUserConsumer(BloomFilterUtils bloomFilterUtils) {
        this.bloomFilterUtils = bloomFilterUtils;
    }

    @Override
    public void onMessage(MessageExt messageExt) {
        if (bloomFilterUtils.mightContainBloomFilter(BloomFilterMap.ROCKETMQ_IDEMPOTENT_BLOOM_FILTER, messageExt.getMsgId())) {
            log.info("消息已经消费过：{}", messageExt.getMsgId());
            return;
        }
        String s = new String(messageExt.getBody());
        log.info("收到消息：{}",s);
        MessageVO message = JSON.parseObject(s, MessageVO.class);
        String messageTo = message.getTo();
        Channel channel = IMServerHandler.USER_CHANNEL_MAP.get(messageTo);
        if(channel!=null){
            log.info("找到用户：{}，发送消息：{}",messageTo,message);
            channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(message)));
        }
        bloomFilterUtils.addBloomFilter(BloomFilterMap.ROCKETMQ_IDEMPOTENT_BLOOM_FILTER, messageExt.getMsgId());
    }
}
