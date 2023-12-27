package com.yyh.xfs.im.server;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.yyh.xfs.im.initialzer.IMServerInitialzer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author yyh
 * @date 2023-12-24
 */
@Component
@Slf4j
public class IMServer {
    @Value("${netty.port}")
    private Integer port;
    @Value("${netty.application.name}")
    private String applicationName;

    private final NacosServiceManager nacosServiceManager;

    private final IMServerInitialzer imServerInitialzer;

    private final NacosDiscoveryProperties nacosDiscoveryProperties;
    private static final EventLoopGroup BOSS_GROUP;
    private static final EventLoopGroup WORKER_GROUP;
    static {
        BOSS_GROUP = new NioEventLoopGroup();
        WORKER_GROUP = new NioEventLoopGroup();
    }
    public IMServer(IMServerInitialzer imServerInitialzer, NacosServiceManager nacosServiceManager, NacosDiscoveryProperties nacosDiscoveryProperties) {
        this.imServerInitialzer = imServerInitialzer;
        this.nacosServiceManager = nacosServiceManager;
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
    }
    /**
     * 将netty服务注册到nacos
     */
    public void registerNetty() throws NacosException, UnknownHostException {
        NamingService namingService = nacosServiceManager.getNamingService(nacosDiscoveryProperties.getNacosProperties());
        Instance instance = new Instance();
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        instance.setIp(hostAddress);
        instance.setPort(port);
        namingService.registerInstance(applicationName,instance);
    }


    /**
     * 启动IM服务器
     */
    public void start(){
        try {
            //服务端进行启动类
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //使用NIO模式，初始化器等等
            serverBootstrap.group(BOSS_GROUP, WORKER_GROUP).channel(NioServerSocketChannel.class).childHandler(imServerInitialzer);
            //绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            registerNetty();
            log.info("tcp服务器已经启动…………");
            // 等待服务端监听端口关闭
            new Thread(() -> {
                try {
                    channelFuture.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    log.error("服务器启动失败",e);
                }
            }).start();
        } catch (InterruptedException | UnknownHostException e) {
            log.error("服务器启动失败",e);
        } catch (NacosException e) {
            log.error("nacos注册失败",e);
        }
    }
    @PreDestroy
    public void close(){
        BOSS_GROUP.shutdownGracefully();
        WORKER_GROUP.shutdownGracefully();
    }
}
