package com.yyh.xfs.search.consumer.user;

import com.alibaba.fastjson.JSON;
import com.yyh.xfs.search.domain.UserEsDO;
import com.yyh.xfs.search.service.UserSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author yyh
 * @date 2024-02-21
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = "user-update-es-topic", consumerGroup = "user-update-es-consumer-group")
public class UserUpdateEsConsumer implements RocketMQListener<String> {
    private final UserSearchService userSearchService;

    public UserUpdateEsConsumer(UserSearchService userSearchService) {
        this.userSearchService = userSearchService;
    }

    @Override
    public void onMessage(String s) {
        log.info("收到消息：{}", s);
        UserEsDO userEsDO = JSON.parseObject(s, UserEsDO.class);
        log.info("转换后的对象：{}", userEsDO);
        userSearchService.updateUser(userEsDO);
    }
}
