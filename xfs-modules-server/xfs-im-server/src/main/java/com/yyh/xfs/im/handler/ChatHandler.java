package com.yyh.xfs.im.handler;

import com.alibaba.fastjson.JSON;
import com.yyh.xfs.common.redis.constant.RedisConstant;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.common.redis.utils.RedisKey;
import com.yyh.xfs.im.domain.Message;
import com.yyh.xfs.im.vo.MessageVO;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.concurrent.ThreadPoolExecutor;

import static com.yyh.xfs.im.handler.IMServerHandler.USER_CHANNEL_MAP;

/**
 * @author yyh
 * @date 2023-12-25
 */
@Component
@Slf4j
public class ChatHandler {
    private final RedisCache redisCache;
    private final RocketMQTemplate rocketMQTemplate;
    private final MongoTemplate mongoTemplate;
    private final ThreadPoolExecutor threadPoolExecutor;

    public ChatHandler(RedisCache redisCache,
                       RocketMQTemplate rocketMQTemplate,
                       MongoTemplate mongoTemplate,
                       ThreadPoolExecutor threadPoolExecutor) {
        this.redisCache = redisCache;
        this.rocketMQTemplate = rocketMQTemplate;
        this.mongoTemplate = mongoTemplate;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    public void execute(MessageVO messageVO) {
        Channel channel = USER_CHANNEL_MAP.get(messageVO.getTo());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        messageVO.setTime(simpleDateFormat.format(System.currentTimeMillis()));
        // 消息持久化到mongodb
        threadPoolExecutor.execute(() -> {
            Message message = new Message();
            BeanUtils.copyProperties(messageVO, message);
            mongoTemplate.insert(message);
        });
        if (channel != null) {
            log.info("双方在一个服务，直接发送消息");
            channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(messageVO)));
            return;
        }
        boolean b = redisCache.sHasKey(RedisConstant.REDIS_KEY_USER_ONLINE, messageVO.getTo());
        if (b) {
            log.info("双方不在一个服务，发送广播消息");
            // 利用rocketmq发送广播消息，让所有的服务都能收到消息，然后再发送给用户
            rocketMQTemplate.convertAndSend("find-user-topic", JSON.toJSONString(messageVO));
            return;
        }
        log.info("对方不在线，发送离线消息");
        // 将离线消息存储到redis中，key为发送者和接收者的id，value为消息
        redisCache.lSet(
                RedisKey.build(RedisConstant.REDIS_KEY_USER_OFFLINE_MESSAGE,messageVO.getFrom()+"-"+messageVO.getTo()),
                JSON.toJSONString(messageVO));
    }
}
