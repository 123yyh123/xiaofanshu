package com.yyh.xfs.im.server;

import com.yyh.xfs.im.initialzer.IMServerInitialzer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author yyh
 * @date 2023-12-24
 */
@Component
public class IMServer {
    public IMServer() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new IMServerInitialzer());
            serverBootstrap.bind(8083).sync();
            System.out.println("Netty启动成功");
        } catch (InterruptedException e) {
            System.out.println("Netty启动失败");
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
