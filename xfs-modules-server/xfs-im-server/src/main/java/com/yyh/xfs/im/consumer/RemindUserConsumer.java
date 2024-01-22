package com.yyh.xfs.im.consumer;

import com.alibaba.fastjson.JSON;
import com.yyh.xfs.common.redis.constant.RedisConstant;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.common.redis.utils.RedisKey;
import com.yyh.xfs.im.handler.types.ChatHandler;
import com.yyh.xfs.im.vo.MessageVO;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.yyh.xfs.im.handler.IMServerHandler.USER_CHANNEL_MAP;

/**
 * @author yyh
 * @date 2024-01-23
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = "notes-remind-target-topic", consumerGroup = "notes-remind-target-consumer-group")
public class RemindUserConsumer implements RocketMQListener<String> {

    private final ChatHandler chatHandler;
    private final RedisCache redisCache;

    public RemindUserConsumer(RedisCache redisCache, ChatHandler chatHandler) {
        this.redisCache = redisCache;
        this.chatHandler = chatHandler;
    }

    @Override
    public void onMessage(String s) {
        log.info("收到消息：{}", s);
        Map<String,Object> map = JSON.parseObject(s, Map.class);
        String userId = (String) map.get("belongUserId");
        Map<String, Object> userInfo = redisCache.hmget(RedisConstant.REDIS_KEY_USER_LOGIN_INFO + userId);
        if (userInfo == null) {
            log.info("用户未登录");
            return;
        }
        String avatarUrl = (String) map.get("avatarUrl");
        String nickName = (String) map.get("nickName");
        MessageVO messageVO = new MessageVO();
        messageVO.setFrom(userId);
        messageVO.setFromAvatar(avatarUrl);
        messageVO.setFromName(nickName);
        messageVO.setTo((String) map.get("toUserId"));
        messageVO.setContent((String) map.get("coverPicture"));
        messageVO.setTime(System.currentTimeMillis());
        messageVO.setMessageType(7);
        messageVO.setChatType(0);
        Channel channel = USER_CHANNEL_MAP.get(messageVO.getTo());
        chatHandler.sendMessage(channel, messageVO);
    }
}
