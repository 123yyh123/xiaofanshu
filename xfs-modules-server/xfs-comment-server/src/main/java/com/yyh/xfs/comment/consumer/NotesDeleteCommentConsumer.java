package com.yyh.xfs.comment.consumer;

import com.yyh.xfs.comment.service.CommentService;
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
@RocketMQMessageListener(topic = "notes-delete-comment-topic", consumerGroup = "notes-delete-comment-consumer-group")
public class NotesDeleteCommentConsumer implements RocketMQListener<String> {

    private final CommentService commentService;

    public NotesDeleteCommentConsumer(CommentService commentService) {
        this.commentService = commentService;
    }

    @Override
    public void onMessage(String s) {
        log.info("收到消息：{}", s);
        Long notesId = Long.valueOf(s);
        log.info("notesId:{}", notesId);
        commentService.deleteCommentByNotesId(notesId);
    }
}
