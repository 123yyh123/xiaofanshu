package com.yyh.xfs.search.consumer.notes;

import com.alibaba.fastjson.JSON;
import com.yyh.xfs.search.service.NotesSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author yyh
 * @date 2024-02-19
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = "notes-update-count-topic", consumerGroup = "notes-update-count-consumer-group")
public class NotesUpdateCountConsumer implements RocketMQListener<String> {
    private final NotesSearchService notesSearchService;

    public NotesUpdateCountConsumer(NotesSearchService notesSearchService) {
        this.notesSearchService = notesSearchService;
    }

    @Override
    public void onMessage(String message) {
        log.info("notes-update-count-topic receive message:{}", message);
        Map<String,String> map = JSON.parseObject(message, Map.class);
        log.info("notesId:{},notesLikeNum:{},type:{}", map.get("notesId"), map.get("notesLikeNum"), map.get("type"));
        notesSearchService.updateCount(map);
    }
}
