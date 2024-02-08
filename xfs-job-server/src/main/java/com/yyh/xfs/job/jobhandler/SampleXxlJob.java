package com.yyh.xfs.job.jobhandler;

import com.mongodb.client.result.UpdateResult;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.yyh.xfs.comment.domain.CommentDO;
import com.yyh.xfs.common.redis.constant.RedisConstant;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.common.redis.utils.RedisKey;
import com.yyh.xfs.job.mapper.NotesMapper;
import com.yyh.xfs.job.service.NotesService;
import com.yyh.xfs.job.service.UserService;
import com.yyh.xfs.user.domain.UserDO;
import com.yyh.xfs.user.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author yyh
 * @date 2023-12-23
 * @desc xfs-job
 */
@Component
@Slf4j
public class SampleXxlJob {
    private final UserService userService;
    private final Executor jobThreadPool;
    private final RedisCache redisCache;
    private final NotesMapper notesMapper;
    private final MongoTemplate mongoTemplate;

    public SampleXxlJob(RedisCache redisCache,
                        Executor jobThreadPool,
                        UserService userService,
                        NotesMapper notesMapper,
                        MongoTemplate mongoTemplate) {
        this.redisCache = redisCache;
        this.jobThreadPool = jobThreadPool;
        this.userService = userService;
        this.notesMapper = notesMapper;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 1、简单任务示例（Bean模式）
     */
    @XxlJob("userInfoJobHandler")
    public void updateUserInfoJobHandler() throws Exception {
        XxlJobHelper.log("start update userInfo");
        // 从redis中获取要更新用户的id
        long l = redisCache.zSetSize(RedisConstant.REDIS_KEY_USER_INFO_UPDATE_LIST);
        XxlJobHelper.log("update userInfo size:{}", l);
        BoundZSetOperations<String, Object> setOps = redisCache.boundZSetOps(RedisConstant.REDIS_KEY_USER_INFO_UPDATE_LIST);
        // 每个线程更新100个用户信息，更新完成后删除，放到线程池中执行，使用多线程更新
        for (int i = 0; i < l / 100 + 1; i++) {
            int finalI = i;
            jobThreadPool.execute(() -> {
                Set<Object> range = setOps.range(finalI, finalI + 100);
                if (range != null) {
                    List<String> collect = range.stream().map(o -> RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, o.toString())).collect(Collectors.toList());
                    List<Object> userInfoList = redisCache.pipelineGet(collect);
                    List<UserVO> userVos = userInfoList.stream().map(o -> {
                        Map<String, Object> map = (Map<String, Object>) o;
                        return new UserVO(map);
                    }).collect(Collectors.toList());
                    userService.updateByVos(userVos);
                    // 删除已经更新的用户
                    setOps.removeRange(finalI, finalI + 100);
                }
            });
        }
    }

    /**
     * 更新笔记的点赞数和收藏数
     */
    @XxlJob("updateNotesCount")
    public void updateNotesCount() {
        XxlJobHelper.log("start update notesCount");
        // 取出所有笔记的点赞和收藏数量键的前缀
        List<String> collect = new ArrayList<>(redisCache.keys(RedisConstant.REDIS_KEY_NOTES_COUNT));
        // 多线程更新，每个线程更新100个笔记
        for (int i = 0; i < collect.size() / 100 + 1; i++) {
            int finalI = i;
            jobThreadPool.execute(() -> {
                List<String> subList = collect.subList(finalI * 100, Math.min((finalI + 1) * 100, collect.size()));
                subList.forEach(key -> {
                    Long notesId = Long.valueOf(key.substring(RedisConstant.REDIS_KEY_NOTES_COUNT.length()));
                    Object notesLikeNum = redisCache.hget(key, "notesLikeNum");
                    Object notesCollectNum = redisCache.hget(key, "notesCollectNum");
                    if (Objects.nonNull(notesLikeNum)) {
                        Integer likeNum = (Integer) notesLikeNum;
                        boolean r = notesMapper.updateNotesLikeNum(notesId, likeNum);
                        if (r) {
                            redisCache.hdel(key, "notesLikeNum");
                        } else {
                            // TODO 进行相应的处理，如rocketmq发送消息
                            log.error("update notesLikeNum error,notesId:{},likeNum:{}", notesId, likeNum);
                        }
                    }
                    if (Objects.nonNull(notesCollectNum)) {
                        Integer collectNum = (Integer) notesCollectNum;
                        boolean r = notesMapper.updateNotesCollectNum(notesId, collectNum);
                        if (r) {
                            redisCache.hdel(key, "notesCollectNum");
                        } else {
                            // TODO 进行相应的处理，如rocketmq发送消息
                            log.error("update notesCollectNum error,notesId:{},collectNum:{}", notesId, collectNum);
                        }
                    }
                });
            });
        }
    }

    /**
     * 更新笔记评论的点赞数
     */
    @XxlJob("updateNotesCommentLikeNum")
    public void updateNotesCommentLikeNum() {
        XxlJobHelper.log("start update notesCommentLikeNum");
        // 取出所有笔记评论的点赞数量键的前缀
        List<String> collect = new ArrayList<>(redisCache.keys(RedisConstant.REDIS_KEY_COMMENT_COUNT));
        // 多线程更新，每个线程更新100个笔记评论
        for (int i = 0; i < collect.size() / 100 + 1; i++) {
            int finalI = i;
            jobThreadPool.execute(() -> {
                List<String> subList = collect.subList(finalI * 100, Math.min((finalI + 1) * 100, collect.size()));
                subList.forEach(key -> {
                    Long notesCommentId = Long.valueOf(key.substring(RedisConstant.REDIS_KEY_COMMENT_COUNT.length()));
                    Object notesCommentLikeNum = redisCache.get(key);
                    if (Objects.nonNull(notesCommentLikeNum)) {
                        Integer likeNum = (Integer) notesCommentLikeNum;
                        // 更新数据库
                        Query query = new Query(Criteria.where("id").is(notesCommentId));
                        Update update = new Update().set("commentLikeNum", likeNum);
                        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, CommentDO.class);
                        if (updateResult.getModifiedCount() > 0) {
                            redisCache.del(key);
                        } else {
                            // TODO 进行相应的处理，如rocketmq发送消息
                            log.error("update notesCommentLikeNum error,notesCommentId:{},likeNum:{}", notesCommentId, likeNum);
                        }
                    }
                });
            });
        }
    }

    /**
     * 更新最近的点赞笔记和收藏笔记
     */
    @XxlJob("updateUserLikeAndCollectNotes")
    public void updateUserLikeAndCollectNotes() {
        XxlJobHelper.log("start updateUserLikeAndCollectNotes");
        // 取出所有用户的点赞和收藏笔记的键
        List<String> collect = new ArrayList<>(redisCache.keys(RedisConstant.REDIS_KEY_USER_LIKE_NOTES_RECENT));
        collect.addAll(redisCache.keys(RedisConstant.REDIS_KEY_USER_COLLECT_NOTES));
        // 多线程更新，每个key一个线程
        collect.forEach(key -> {
            jobThreadPool.execute(() -> {
                Set<Object> userLikeNotesSet = redisCache.sGet(key);
                if (userLikeNotesSet != null && !userLikeNotesSet.isEmpty()) {
                    userLikeNotesSet.forEach(userLikeNotes -> {
                        Map<String, Object> userLikeNotesMap = (Map<String, Object>) userLikeNotes;
                        Boolean isLike = (Boolean) userLikeNotesMap.get("isLike");
                        if (isLike) {
                            Long notesId = (Long) userLikeNotesMap.get("notesId");
                            Long userId = (Long) userLikeNotesMap.get("userId");
                            // 更新数据库
                            Query query = new Query(Criteria.where("id").is(notesId));
                            Update update = new Update().addToSet("likeUserIds", userId);
                            mongoTemplate.updateFirst(query, update, CommentDO.class);
                        }
                    });
                }
            });
        });
    }
}