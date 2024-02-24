package com.yyh.xfs.job.jobhandler;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.mongodb.client.result.UpdateResult;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.yyh.xfs.comment.domain.CommentDO;
import com.yyh.xfs.common.redis.constant.BloomFilterMap;
import com.yyh.xfs.common.redis.constant.RedisConstant;
import com.yyh.xfs.common.redis.utils.BloomFilterUtils;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.common.redis.utils.RedisKey;
import com.yyh.xfs.job.mapper.notes.NotesMapper;
import com.yyh.xfs.job.mapper.user.UserMapper;
import com.yyh.xfs.job.service.NotesService;
import com.yyh.xfs.notes.domain.UserCollectNotesDO;
import com.yyh.xfs.notes.domain.UserLikeNotesDO;
import com.yyh.xfs.user.domain.UserDO;
import com.yyh.xfs.user.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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
    private final UserMapper userMapper;
    private final Executor jobThreadPool;
    private final RedisCache redisCache;
    private final NotesMapper notesMapper;
    private final MongoTemplate mongoTemplate;
    private final NotesService notesService;

    private final BloomFilterUtils bloomFilterUtils;

    public SampleXxlJob(RedisCache redisCache,
                        @Qualifier("asyncThreadExecutor") Executor jobThreadPool,
                        NotesMapper notesMapper,
                        MongoTemplate mongoTemplate,
                        UserMapper userMapper, NotesService notesService,
                        BloomFilterUtils bloomFilterUtils) {
        this.redisCache = redisCache;
        this.jobThreadPool = jobThreadPool;
        this.notesMapper = notesMapper;
        this.mongoTemplate = mongoTemplate;
        this.userMapper = userMapper;
        this.notesService = notesService;
        this.bloomFilterUtils = bloomFilterUtils;
    }

    /**
     * 更新用户信息
     */
    @XxlJob("userInfoJobHandler")
    public void updateUserInfoJobHandler() {
        XxlJobHelper.log("start update userInfo");
        // 从redis中获取要更新用户的id
        Set<Object> objects = redisCache.sGet(RedisConstant.REDIS_KEY_USER_INFO_UPDATE_LIST);
        // 每次更新100个用户，多线程更新
        for (int i = 0; i < objects.size() / 100 + 1; i++) {
            int finalI = i;
            jobThreadPool.execute(() -> {
                List<Object> subList = new ArrayList<>(objects).subList(finalI * 100, Math.min((finalI + 1) * 100, objects.size()));
                subList.forEach(o -> {
                    // 从redis中获取用户信息
                    Map<String, Object> map = redisCache.hmget(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, o.toString()));
                    if (map.isEmpty()) {
                        return;
                    }
                    UserDO userDO = UserDO.voToDO(new UserVO(map));
                    // 更新数据库
                    userMapper.updateUser(userDO);
                    // 删除redis中的更新标记
                    redisCache.setRemove(RedisConstant.REDIS_KEY_USER_INFO_UPDATE_LIST, o);
                });
            });
        }
    }

    /**
     * 更新笔记的点赞数，收藏数和浏览数
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
                    Object notesCollectionNum = redisCache.hget(key, "notesCollectionNum");
                    Object notesViewNum = redisCache.hget(key, "notesViewNum");
                    if (Objects.nonNull(notesLikeNum)) {
                        notesService.updateNotesLikeNum(key, notesId, (Integer) notesLikeNum);
                    }
                    if (Objects.nonNull(notesCollectionNum)) {
                        notesService.updateNotesCollectionNum(key, notesId, (Integer) notesCollectionNum);
                    }
                    if (Objects.nonNull(notesViewNum)) {
                        notesService.updateNotesViewNum(key, notesId, (Integer) notesViewNum);
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
                    String notesCommentId = key.substring(RedisConstant.REDIS_KEY_COMMENT_COUNT.length());
                    Object notesCommentLikeNum = redisCache.get(key);
                    if (Objects.nonNull(notesCommentLikeNum)) {
                        Integer likeNum = (Integer) notesCommentLikeNum;
                        // 更新数据库
                        ObjectId objectId = new ObjectId(notesCommentId);
                        Query query = new Query(Criteria.where("_id").is(objectId));
                        Update update = new Update().set("commentLikeNum", likeNum);
                        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, CommentDO.class);
                        if (updateResult.wasAcknowledged()) {
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
     * 更新最近的点赞笔记
     */
    @XxlJob("updateUserLikeNotes")
    public void updateUserLikeNotes() {
        XxlJobHelper.log("start updateUserLikeNotes");
        // 取出所有用户的点赞笔记的键
        List<String> collect = new ArrayList<>(redisCache.keys(RedisConstant.REDIS_KEY_USER_LIKE_NOTES_RECENT));
        // 多线程更新，每个key一个线程
        collect.forEach(key -> jobThreadPool.execute(() -> redisCache.rangeZSet(key, 0, -1).forEach(o -> {
            // 获取分数
            long time = redisCache.zSetScore(key, o).longValue();
            // 更新数据库
            Map<String, Object> map = JSON.parseObject(o.toString(), Map.class);
            UserLikeNotesDO userLikeNotesDO = new UserLikeNotesDO();
            userLikeNotesDO.setUserId(Long.valueOf(map.get("userId").toString()));
            userLikeNotesDO.setNotesId(Long.valueOf(map.get("notesId").toString()));
            userLikeNotesDO.setCreateTime(new Date(time));
            Boolean isLike = (Boolean) map.get("isLike");
            if (isLike) {
                long id = IdWorker.getId(userLikeNotesDO);
                userLikeNotesDO.setId(id);
                if (notesMapper.insertUserLikeNotes(userLikeNotesDO)) {
                    redisCache.removeZSet(key, o);
                } else {
                    // TODO 进行相应的处理，如rocketmq发送消息
                    log.error("insert userLikeNotes error,userLikeNotesDO:{}", userLikeNotesDO);
                }
            } else {
                // 不需要观察返回值，因为有可能没有点赞记录
                notesMapper.deleteUserLikeNotes(userLikeNotesDO);
            }
        })));
    }

    /**
     * 更新最近的收藏笔记
     */
    @XxlJob("updateUserCollectNotes")
    public void updateUserCollectNotes() {
        XxlJobHelper.log("start updateUserCollectNotes");
        // 取出所有用户的收藏笔记的键
        List<String> collect = new ArrayList<>(redisCache.keys(RedisConstant.REDIS_KEY_USER_COLLECT_NOTES_RECENT));
        // 多线程更新，每个key一个线程
        collect.forEach(key -> jobThreadPool.execute(() -> redisCache.rangeZSet(key, 0, -1).forEach(o -> {
            // 获取分数
            long time = redisCache.zSetScore(key, o).longValue();
            // 更新数据库
            Map<String, Object> map = JSON.parseObject(o.toString(), Map.class);
            UserCollectNotesDO userCollectNotesDO = new UserCollectNotesDO();
            userCollectNotesDO.setUserId(Long.valueOf(map.get("userId").toString()));
            userCollectNotesDO.setNotesId(Long.valueOf(map.get("notesId").toString()));
            userCollectNotesDO.setCreateTime(new Date(time));
            Boolean isCollect = (Boolean) map.get("isCollect");
            if (isCollect) {
                long id = IdWorker.getId(userCollectNotesDO);
                userCollectNotesDO.setId(id);
                if (notesMapper.insertUserCollectNotes(userCollectNotesDO)) {
                    redisCache.removeZSet(key, o);
                } else {
                    // TODO 进行相应的处理，如rocketmq发送消息
                    log.error("insert userCollectNotes error,userCollectNotesDO:{}", userCollectNotesDO);
                }
            } else {
                // 不需要观察返回值，因为有可能没有收藏记录
                notesMapper.deleteUserCollectNotes(userCollectNotesDO);
            }
        })));
    }

    /**
     * 检查布隆过滤器元素是否超过预期，不能超过3/4，超过则重新初始化
     */
    @XxlJob("checkBloomFilter")
    public void checkBloomFilter() {
        XxlJobHelper.log("start checkBloomFilter");
        // 获取笔记id的布隆过滤器
        long currentNotesIdNum = bloomFilterUtils.getBloomFilterSize(BloomFilterMap.NOTES_ID_BLOOM_FILTER);
        // 获取笔记id的数量
        long expectedNotesInsertNum = bloomFilterUtils.getExpectedInsertionsBloomFilter(BloomFilterMap.NOTES_ID_BLOOM_FILTER);
        if (expectedNotesInsertNum==0||currentNotesIdNum >= expectedNotesInsertNum * 3 / 4) {
            // 重新初始化布隆过滤器
            List<String> ids = notesMapper.getAllNotesId().stream().map(String::valueOf).collect(Collectors.toList());
            bloomFilterUtils.initBloomFilter(BloomFilterMap.NOTES_ID_BLOOM_FILTER, ids.isEmpty() ? 10000 : ids.size() * 4L, 0.01);
            // 多线程初始化，每个线程初始化1000个id
            for (int i = 0; i < ids.size() / 1000 + 1; i++) {
                int finalI = i;
                jobThreadPool.execute(() -> {
                    List<String> subList = ids.subList(finalI * 1000, Math.min((finalI + 1) * 1000, ids.size()));
                    bloomFilterUtils.addAllBloomFilter(BloomFilterMap.NOTES_ID_BLOOM_FILTER, subList);
                });
            }
        }
        long currentUserIdNum = bloomFilterUtils.getBloomFilterSize(BloomFilterMap.USER_ID_BLOOM_FILTER);
        long expectedUserInsertNum = bloomFilterUtils.getExpectedInsertionsBloomFilter(BloomFilterMap.USER_ID_BLOOM_FILTER);
        if (expectedUserInsertNum==0||currentUserIdNum >= expectedUserInsertNum * 3 / 4) {
            // 重新初始化布隆过滤器
            List<String> ids = userMapper.getAllUserId().stream().map(String::valueOf).collect(Collectors.toList());
            bloomFilterUtils.initBloomFilter(BloomFilterMap.USER_ID_BLOOM_FILTER, ids.isEmpty() ? 10000 : ids.size() * 4L, 0.01);
            // 多线程初始化，每个线程初始化1000个id
            for (int i = 0; i < ids.size() / 1000 + 1; i++) {
                int finalI = i;
                jobThreadPool.execute(() -> {
                    List<String> subList = ids.subList(finalI * 1000, Math.min((finalI + 1) * 1000, ids.size()));
                    bloomFilterUtils.addAllBloomFilter(BloomFilterMap.USER_ID_BLOOM_FILTER, subList);
                });
            }
        }
    }
}