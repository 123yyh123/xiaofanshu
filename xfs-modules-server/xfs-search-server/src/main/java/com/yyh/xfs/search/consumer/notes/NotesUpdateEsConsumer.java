package com.yyh.xfs.search.consumer.notes;

import com.alibaba.fastjson.JSON;
import com.yyh.xfs.notes.domain.NotesDO;
import com.yyh.xfs.search.service.NotesSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yyh
 * @date 2024-02-17
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = "notes-update-es-topic", consumerGroup = "notes-update-es-consumer-group")
public class NotesUpdateEsConsumer implements RocketMQListener<String> {

    private final NotesSearchService notesSearchService;

    public NotesUpdateEsConsumer(NotesSearchService notesSearchService) {
        this.notesSearchService = notesSearchService;
    }

    @Override
    public void onMessage(String s) {
        log.info("收到消息：{}", s);
        NotesDO notesDO = JSON.parseObject(s, NotesDO.class);
        log.info("notesDO:{}", notesDO);
        notesSearchService.updateNotes(notesDO);
    }
}
