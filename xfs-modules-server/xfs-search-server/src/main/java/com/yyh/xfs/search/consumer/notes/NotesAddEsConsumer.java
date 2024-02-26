package com.yyh.xfs.search.consumer.notes;

import com.alibaba.fastjson.JSON;
import com.yyh.xfs.common.constant.RocketMQConsumerGroupConstant;
import com.yyh.xfs.common.constant.RocketMQTopicConstant;
import com.yyh.xfs.notes.domain.NotesDO;
import com.yyh.xfs.search.service.NotesSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author yyh
 * @date 2024-01-24
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMQTopicConstant.NOTES_ADD_ES_TOPIC,
        consumerGroup = RocketMQConsumerGroupConstant.NOTES_ADD_ES_CONSUMER_GROUP)
public class NotesAddEsConsumer implements RocketMQListener<String> {

    private final NotesSearchService notesSearchService;

    public NotesAddEsConsumer(NotesSearchService notesSearchService) {
        this.notesSearchService = notesSearchService;
    }

    @Override
    public void onMessage(String s) {
        log.info("收到消息：{}", s);
        NotesDO notesDO = JSON.parseObject(s, NotesDO.class);
        log.info("notesDO:{}", notesDO);
        notesSearchService.addNotes(notesDO);
    }
}
