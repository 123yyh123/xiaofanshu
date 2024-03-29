package com.yyh.xfs.search.consumer.notes;

import com.yyh.xfs.common.constant.RocketMQConsumerGroupConstant;
import com.yyh.xfs.common.constant.RocketMQTopicConstant;
import com.yyh.xfs.search.service.NotesSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author yyh
 * @date 2024-02-17
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMQTopicConstant.NOTES_DELETE_ES_TOPIC,
        consumerGroup = RocketMQConsumerGroupConstant.NOTES_DELETE_ES_CONSUMER_GROUP)
public class NotesDeleteEsConsumer implements RocketMQListener<String> {

    private final NotesSearchService notesSearchService;

    public NotesDeleteEsConsumer(NotesSearchService notesSearchService) {
        this.notesSearchService = notesSearchService;
    }

    @Override
    public void onMessage(String s) {
        log.info("收到消息：{}", s);
        Long notesId = Long.valueOf(s);
        log.info("notesId:{}", notesId);
        notesSearchService.deleteNotes(notesId);
    }
}
