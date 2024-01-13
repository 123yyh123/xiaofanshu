package com.yyh.xfs.im.handler.types;

import com.alibaba.fastjson.JSON;
import com.yyh.xfs.common.redis.constant.RedisConstant;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.common.redis.utils.RedisKey;
import com.yyh.xfs.im.domain.MessageDO;
import com.yyh.xfs.im.mapper.UserAttentionMapper;
import com.yyh.xfs.im.mapper.UserBlackMapper;
import com.yyh.xfs.im.vo.MessageVO;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import static com.yyh.xfs.im.handler.IMServerHandler.USER_CHANNEL_MAP;

/**
 * @author yyh
 * @date 2023-12-25
 * @desc 聊天处理器
 */
@Component
@Slf4j
public class ChatHandler {
    private final RedisCache redisCache;
    private final RocketMQTemplate rocketMQTemplate;
    private final MongoTemplate mongoTemplate;
    private final Executor asyncThreadExecutor;
    private final UserBlackMapper userBlackMapper;
    private final UserAttentionMapper userAttentionMapper;
    public ChatHandler(RedisCache redisCache,
                       RocketMQTemplate rocketMQTemplate,
                       MongoTemplate mongoTemplate,
                       Executor asyncThreadExecutor,
                       UserBlackMapper userBlackMapper,
                       UserAttentionMapper userAttentionMapper) {
        this.redisCache = redisCache;
        this.rocketMQTemplate = rocketMQTemplate;
        this.mongoTemplate = mongoTemplate;
        this.asyncThreadExecutor = asyncThreadExecutor;
        this.userBlackMapper = userBlackMapper;
        this.userAttentionMapper = userAttentionMapper;
    }

    public void execute(MessageVO messageVO) {
        Channel channel = USER_CHANNEL_MAP.get(messageVO.getTo());
        // TODO 判断双方是否可以互相聊天
        boolean isExist = redisCache.hasKey(RedisKey.build(RedisConstant.REDIS_KEY_USER_RELATION_ALLOW_SEND_MESSAGE,
                messageVO.getFrom() + ":" + messageVO.getTo()));
        if (isExist) {
            Map<String, Object> hmget = redisCache.hmget(RedisKey.build(RedisConstant.REDIS_KEY_USER_RELATION_ALLOW_SEND_MESSAGE,
                    messageVO.getFrom() + ":" + messageVO.getTo()));
            Boolean isBlacked = (Boolean) hmget.get("isBlacked");
            if (isBlacked) {
                log.info("用户{}被用户{}拉黑，无法发送消息", messageVO.getFrom(), messageVO.getTo());
                // 告知发送者，对方已经将你拉黑
                messageVO.setContent("对方已将你拉黑");
                replyMessage(USER_CHANNEL_MAP.get(messageVO.getFrom()), messageVO);
                return;
            }
            Boolean allowSendMessage = (Boolean) hmget.get("allowSendMessage");
            if (!allowSendMessage) {
                log.info("对方没有关注，用户{}24小时内已经向用户{}发送消息", messageVO.getFrom(), messageVO.getTo());
                // 告知发送者，对方没有关注你，24小时内只能发送一条消息
                messageVO.setContent("对方没有关注你，24小时内只能发送一条消息");
                replyMessage(USER_CHANNEL_MAP.get(messageVO.getFrom()), messageVO);
                return;
            }
            sendMessage(channel, messageVO);
            // 应答消息，告知发送者，消息发送成功
            MessageVO replyMessage = new MessageVO();
            replyMessage.setFrom(messageVO.getFrom());
            replyMessage.setTo(messageVO.getTo());
            replyMessage.setId(messageVO.getId());
            replyMessage(USER_CHANNEL_MAP.get(messageVO.getFrom()), replyMessage);
            return;
        }
        createKeyAndSendMessage(channel, messageVO);
    }

    private void createKeyAndSendMessage(Channel channel, MessageVO messageVO) {
        Map<String, Object> userRelation = new HashMap<>();
        // 判断对方是否拉黑了自己
        Boolean isBlack = userBlackMapper.selectOneByUserIdAndBlackIdIsExist
                (Long.valueOf(messageVO.getTo()), Long.valueOf(messageVO.getFrom()));
        if (isBlack) {
            log.info("用户{}被用户{}拉黑，无法发送消息", messageVO.getFrom(), messageVO.getTo());
            userRelation.putIfAbsent("isBlacked", true);
            userRelation.putIfAbsent("allowSendMessage", false);
            // 告知发送者，对方已经将你拉黑
            messageVO.setContent("对方已将你拉黑");
            replyMessage(USER_CHANNEL_MAP.get(messageVO.getFrom()), messageVO);
        } else {
            // 判断对方是否关注了我
            Boolean isAttention = userAttentionMapper.selectOneByUserIdAndAttentionIdIsExist(
                    Long.valueOf(messageVO.getTo()), Long.valueOf(messageVO.getFrom()));
            if (isAttention) {
                log.info("对方关注了我，可以发送消息");
                userRelation.putIfAbsent("isBlacked", false);
                userRelation.putIfAbsent("allowSendMessage", true);

            } else {
                log.info("对方没有关注我，用户{}24小时内只能向用户{}发送一条消息", messageVO.getFrom(), messageVO.getTo());
                userRelation.putIfAbsent("isBlacked", false);
                userRelation.putIfAbsent("allowSendMessage", false);
                // 只发送这一次消息，下次只能等到redis中的key过期后才能发送，即24小时
            }
            sendMessage(channel, messageVO);
            // 应答消息，告知发送者，消息发送成功
            MessageVO replyMessage = new MessageVO();
            replyMessage.setFrom(messageVO.getFrom());
            replyMessage.setTo(messageVO.getTo());
            replyMessage.setId(messageVO.getId());
            replyMessage(USER_CHANNEL_MAP.get(messageVO.getFrom()), replyMessage);
        }
        // 将用户关系存储到redis中，key为发送者和接收者的id，value为用户关系
        redisCache.hmset(RedisKey.build(RedisConstant.REDIS_KEY_USER_RELATION_ALLOW_SEND_MESSAGE,
                messageVO.getFrom() + ":" + messageVO.getTo()), userRelation, 24 * 60 * 60);
    }
    private void sendMessage(Channel channel, MessageVO messageVO) {
        messageVO.setTime(System.currentTimeMillis());
        // 消息持久化到mongodb，异步执行
        asyncThreadExecutor.execute(() -> {
            try {
                MessageDO message = new MessageDO();
                BeanUtils.copyProperties(messageVO, message);
                mongoTemplate.insert(message);
            } catch (Exception e) {
                log.error("消息持久化失败", e);
            }
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
                RedisKey.build(RedisConstant.REDIS_KEY_USER_OFFLINE_MESSAGE, messageVO.getTo() + ":" + messageVO.getFrom()),
                JSON.toJSONString(messageVO));
    }

    private void replyMessage(Channel channel, MessageVO messageVO) {
        messageVO.setTime(System.currentTimeMillis());
        messageVO.setChatType(0);
        messageVO.setMessageType(5);
        channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(messageVO)));
    }
}
