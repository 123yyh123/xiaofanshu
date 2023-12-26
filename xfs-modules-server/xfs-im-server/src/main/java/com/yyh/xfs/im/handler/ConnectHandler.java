package com.yyh.xfs.im.handler;

import com.yyh.xfs.common.redis.constant.RedisConstant;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.im.vo.MessageVO;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.yyh.xfs.im.handler.IMServerHandler.USER_CHANNEL_MAP;

/**
 * @author yyh
 * @date 2023-12-25
 */
@Component
@Slf4j
public class ConnectHandler {
    private final RedisCache redisCache;
    public ConnectHandler(RedisCache redisCache) {
        this.redisCache = redisCache;
    }
    public void execute(ChannelHandlerContext channelHandlerContext, MessageVO message) {
        log.info("用户{}连接成功", message.getFrom());
        // 用户上线时，都会发送一条连接信息，将用户和channel绑定
        USER_CHANNEL_MAP.putIfAbsent(message.getFrom(), channelHandlerContext.channel());
        // 将在线用户放到redis中维护，存放的是用户id，方便后面观察是否存储离线消息
        redisCache.sSet(RedisConstant.REDIS_KEY_USER_ONLINE, message.getFrom());
    }
}
