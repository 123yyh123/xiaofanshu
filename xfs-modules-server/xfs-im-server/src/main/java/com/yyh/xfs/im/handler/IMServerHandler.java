package com.yyh.xfs.im.handler;

import com.alibaba.fastjson.JSON;
import com.yyh.xfs.common.myEnum.MessageTypeEnum;
import com.yyh.xfs.common.redis.constant.RedisConstant;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.im.vo.MessageVO;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author yyh
 * @date 2023-12-24
 */
@Component
@ChannelHandler.Sharable
public class IMServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    public static final Map<String, Channel> USER_CHANNEL_MAP = new ConcurrentHashMap<>();

    public static IMServerHandler imServerHandler;

    @Autowired
    private ConnectHandler connectHandler;
    @Autowired
    private ChatHandler chatHandler;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    //2.初始化构造方法一定要有
    public IMServerHandler() {
    }

    //3.容器初始化的时候进行执行-这里是重点
    @PostConstruct
    public void init() {
        imServerHandler = this;
        imServerHandler.connectHandler = this.connectHandler;
        imServerHandler.chatHandler = this.chatHandler;
        imServerHandler.redisCache = this.redisCache;
        imServerHandler.threadPoolExecutor = this.threadPoolExecutor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame frame) throws Exception {
        System.out.println(frame.text());
        try {
            MessageVO message = JSON.parseObject(frame.text(), MessageVO.class);
            Integer type = message.getMessageType();
            if (Objects.equals(type, MessageTypeEnum.CONNECT_MESSAGE)) {
                connectHandler.execute(channelHandlerContext,message);
            } else if (Objects.equals(type, MessageTypeEnum.SYSTEM_MESSAGE)) {
                // TODO 系统消息
            } else if (Objects.equals(type, MessageTypeEnum.CHAT_MESSAGE)) {
               chatHandler.execute(message);
            }else if(Objects.equals(type,MessageTypeEnum.ADD_FRIEND_MESSAGE)) {
                // TODO 添加好友消息
            }
        } catch (Exception e) {
            System.out.println("消息格式错误");
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        Channel channel = ctx.channel();
        // 从用户通道关系中移除，使用线程池异步执行
        threadPoolExecutor.execute(() -> {
            for (Map.Entry<String, Channel> entry : USER_CHANNEL_MAP.entrySet()) {
                if (entry.getValue() == channel) {
                    USER_CHANNEL_MAP.remove(entry.getKey());
                    break;
                }
            }
        });
        redisCache.setRemove(RedisConstant.REDIS_KEY_USER_ONLINE);
        System.out.println("客户端断开连接");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        System.out.println("客户端连接");
    }
}
