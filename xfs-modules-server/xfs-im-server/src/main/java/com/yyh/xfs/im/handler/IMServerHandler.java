package com.yyh.xfs.im.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yyh
 * @date 2023-12-24
 */
public class IMServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    public static final Map<String, Channel> USER_CHANNEL_MAP = new ConcurrentHashMap<>();
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame frame) throws Exception {
        System.out.println(frame.text());
        channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame("服务器返回消息"));
        System.out.println(channelHandlerContext.channel().localAddress());
        System.out.println(channelHandlerContext.channel().id());
    }
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        System.out.println("客户端断开连接");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        System.out.println("客户端连接");
    }
}
