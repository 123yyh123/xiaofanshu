package com.yyh.xfs.common.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @author yyh
 * @date 2023-12-23
 * 线程池配置，该模块主要执行IO操作，所以用IO密集型的线程池较好
 */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "thread.pool.config")
public class ThreadPoolConfig implements AsyncConfigurer {
    /**
     * 核心线程数，IO密集型，一般设置为CPU核心数的2倍
     */
    private int corePoolSize=Runtime.getRuntime().availableProcessors()*2;
    /**
     * 最大线程数，默认与核心线程数一致
     */
    private int maxPoolSize=corePoolSize;
    /**
     * 线程空闲后的最大存活时间，默认60
     */
    private int keepAliveSeconds= 60;
    /**
     * 队列大小，默认100
     */
    private int queueCapacity= 100;
    /**
     * 线程池中的线程的名称前缀，默认thread-pool-
     */
    private String threadNamePrefix= "thread-pool-";
    /**
     * 线程池关闭时等待所有任务都完成后，再继续销毁其他的Bean，默认false
     */
    private boolean waitForTasksToCompleteOnShutdown=false;
    /**
     * 线程池中任务的等待时间，如果超过这个时间还没有销毁就强制销毁，以确保应用最后能够被关闭，而不是阻塞住，默认60
     */
    private int awaitTerminationSeconds= 60;
    @Bean(name = "asyncThreadExecutor")
    public Executor asyncThreadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(corePoolSize);
        // 最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        // 队列大小
        executor.setQueueCapacity(queueCapacity);
        // 线程池中的线程的名称前缀
        executor.setThreadNamePrefix(threadNamePrefix);
        // 线程空闲后的最大存活时间
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // 线程池关闭时等待所有任务都完成后，再继续销毁其他的Bean
        executor.setWaitForTasksToCompleteOnShutdown(waitForTasksToCompleteOnShutdown);
        // 线程池中任务的等待时间，如果超过这个时间还没有销毁就强制销毁，以确保应用最后能够被关闭，而不是阻塞住
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        // 线程池对拒绝任务的处理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化线程池
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> log.error(
                "线程池执行任务方法：{}，异常信息：{}", method.getName(), throwable.getMessage());
    }
}
