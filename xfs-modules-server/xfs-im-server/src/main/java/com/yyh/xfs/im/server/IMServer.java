package com.yyh.xfs.im.server;

import com.yyh.xfs.im.initialzer.IMServerInitialzer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author yyh
 * @date 2023-12-24
 */
@Component
@Slf4j
public class IMServer {
    @Value("${netty.port}")
    private Integer port;
    @Autowired
    private IMServerInitialzer imServerInitialzer;
    private static EventLoopGroup bossGroup;
    private static EventLoopGroup workerGroup;
    static {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
    }
    public void start(){
        try {
            //服务端进行启动类
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //使用NIO模式，初始化器等等
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(imServerInitialzer);
            //绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            log.info("tcp服务器已经启动…………");
            new Thread(() -> {
                try {
                    channelFuture.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (InterruptedException e) {
            System.out.println("服务器启动失败");
        }
    }
    @PreDestroy
    public void close(){
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
