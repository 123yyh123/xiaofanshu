package com.yyh.xfs.job.service.impl;

import com.alibaba.fastjson.JSON;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.job.mapper.notes.NotesMapper;
import com.yyh.xfs.job.service.NotesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yyh
 * @date 2024-02-08
 */
@Service
@Slf4j
public class NotesServiceImpl implements NotesService {
    private final NotesMapper notesMapper;
    private final RedisCache redisCache;
    private final RocketMQTemplate rocketMQTemplate;
    public NotesServiceImpl(NotesMapper notesMapper,
                            RedisCache redisCache,
                            RocketMQTemplate rocketMQTemplate) {
        this.notesMapper = notesMapper;
        this.redisCache = redisCache;
        this.rocketMQTemplate = rocketMQTemplate;
    }
    @Override
    public void updateNotesLikeNum(String key,Long notesId, Integer notesLikeNum) {
        boolean r = notesMapper.updateNotesLikeNum(notesId, notesLikeNum);
        Map<String,String> map=new HashMap<>();
        map.put("notesId",notesId.toString());
        map.put("notesLikeNum",notesLikeNum.toString());
        map.put("type","like");
        rocketMQTemplate.asyncSend("notes-update-count-topic", JSON.toJSONString(map), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("send success");
            }
            @Override
            public void onException(Throwable throwable) {
                log.error("send error");
            }
        });
        if (r) {
            redisCache.hdel(key, "notesLikeNum");
        } else {
            // TODO 进行相应的处理，如rocketmq发送消息
            log.error("update notesLikeNum error,notesId:{},likeNum:{}", notesId, notesLikeNum);
        }
    }

    @Override
    public void updateNotesCollectionNum(String key, Long notesId, Integer notesCollectionNum) {
        boolean r = notesMapper.updateNotesCollectionNum(notesId, notesCollectionNum);
        Map<String,String> map=new HashMap<>();
        map.put("notesId",notesId.toString());
        map.put("notesCollectionNum",notesCollectionNum.toString());
        map.put("type","collection");
        rocketMQTemplate.asyncSend("notes-update-count-topic", JSON.toJSONString(map), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("send success");
            }
            @Override
            public void onException(Throwable throwable) {
                log.error("send error");
            }
        });
        if (r) {
            redisCache.hdel(key, "notesCollectionNum");
        } else {
            // TODO 进行相应的处理，如rocketmq发送消息
            log.error("update notesCollectionNum error,notesId:{},collectionNum:{}", notesId, notesCollectionNum);
        }
    }

    @Override
    public void updateNotesViewNum(String key, Long notesId, Integer notesViewNum) {
        boolean r = notesMapper.updateNotesViewNum(notesId, notesViewNum);
        if (r) {
            redisCache.hdel(key, "notesViewNum");
        } else {
            // TODO 进行相应的处理，如rocketmq发送消息
            log.error("update notesViewNum error,notesId:{},viewNum:{}", notesId, notesViewNum);
        }
    }
}
