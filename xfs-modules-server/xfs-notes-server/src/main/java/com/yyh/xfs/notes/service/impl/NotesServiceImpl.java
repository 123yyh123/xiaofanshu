package com.yyh.xfs.notes.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyh.xfs.common.constant.RocketMQTopicConstant;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.redis.constant.BloomFilterMap;
import com.yyh.xfs.common.redis.constant.RedisConstant;
import com.yyh.xfs.common.redis.utils.BloomFilterUtils;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.common.redis.utils.RedisKey;
import com.yyh.xfs.common.utils.CodeUtil;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.common.web.exception.BusinessException;
import com.yyh.xfs.common.web.utils.AddressUtil;
import com.yyh.xfs.common.web.utils.JWTUtil;
import com.yyh.xfs.notes.domain.*;
import com.yyh.xfs.notes.dto.ResourcesDTO;
import com.yyh.xfs.notes.feign.UserFeign;
import com.yyh.xfs.notes.mapper.*;
import com.yyh.xfs.notes.service.NotesService;
import com.yyh.xfs.notes.utils.NotesUtils;
import com.yyh.xfs.notes.vo.NotesPageVO;
import com.yyh.xfs.notes.vo.NotesPublishVO;
import com.yyh.xfs.notes.vo.NotesVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author yyh
 * @date 2024-01-22
 */
@Service
@Slf4j
public class NotesServiceImpl extends ServiceImpl<NotesMapper, NotesDO> implements NotesService {
    private final RocketMQTemplate rocketMQTemplate;
    private final NotesCategoryMapper notesCategoryMapper;
    private final NotesTopicRelationMapper notesTopicRelationMapper;
    private final NotesTopicMapper notesTopicMapper;
    private final UserLikeNotesMapper userLikeNotesMapper;
    private final UserCollectNotesMapper userCollectNotesMapper;
    private final RedisCache redisCache;
    private final UserFeign userFeign;
    private final HttpServletRequest request;

    private final BloomFilterUtils bloomFilterUtils;

    public NotesServiceImpl(NotesTopicMapper notesTopicMapper,
                            NotesTopicRelationMapper notesTopicRelationMapper,
                            NotesCategoryMapper notesCategoryMapper,
                            RocketMQTemplate rocketMQTemplate,
                            UserFeign userFeign,
                            UserLikeNotesMapper userLikeNotesMapper,
                            UserCollectNotesMapper userCollectNotesMapper,
                            RedisCache redisCache,
                            HttpServletRequest request,
                            BloomFilterUtils bloomFilterUtils) {
        this.notesTopicMapper = notesTopicMapper;
        this.notesTopicRelationMapper = notesTopicRelationMapper;
        this.notesCategoryMapper = notesCategoryMapper;
        this.rocketMQTemplate = rocketMQTemplate;
        this.userFeign = userFeign;
        this.userLikeNotesMapper = userLikeNotesMapper;
        this.userCollectNotesMapper = userCollectNotesMapper;
        this.redisCache = redisCache;
        this.request = request;
        this.bloomFilterUtils = bloomFilterUtils;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> addNotes(NotesPublishVO notesPublishVO) {
        log.info("notesVO:{}", notesPublishVO);
        List<String> notesResources = JSON.parseObject(notesPublishVO.getNotesResources(), List.class);
        log.info("notesResources:{}", notesResources);
        notesParameterCheck(notesPublishVO, notesResources);
        // 设置封面图片
        if (notesPublishVO.getNotesType() == 0) {
            // 图片笔记
            notesPublishVO.setCoverPicture(notesResources.get(0));
        } else {
            if (!StringUtils.hasText(notesPublishVO.getCoverPicture())) {
                notesPublishVO.setCoverPicture(notesResources.get(0) + "?x-oss-process=video/snapshot,t_0,f_jpg,w_0,h_0,m_fast");
            }
        }
        log.info("coverPicture:{}", notesPublishVO.getCoverPicture());
        NotesDO notesDO = new NotesDO();
        BeanUtils.copyProperties(notesPublishVO, notesDO);
        if (notesPublishVO.getBelongCategory() == null) {
            // 利用百度AI接口进行文章分类
            String category = NotesUtils.createCategory(notesPublishVO.getTitle(), notesPublishVO.getRealContent());
            log.info("category:{}", category);
            NotesCategoryDO notesCategoryDO = notesCategoryMapper.selectOne(new QueryWrapper<NotesCategoryDO>().lambda().eq(NotesCategoryDO::getCategoryName, category));
            if (Objects.isNull(notesCategoryDO)) {
                // 默认分类
                notesDO.setBelongCategory(1);
            } else {
                notesDO.setBelongCategory(notesCategoryDO.getId());
            }
        }
        // 设置省份
        try {
            String p = AddressUtil.getAddress(notesPublishVO.getLongitude(), notesPublishVO.getLatitude());
            notesDO.setProvince(p);
        } catch (Exception e) {
            log.error("获取省份失败", e);
            notesDO.setProvince("未知");
        }
        // 保存笔记
        this.baseMapper.insert(notesDO);
        // 将笔记id添加到布隆过滤器中
        bloomFilterUtils.addBloomFilter(BloomFilterMap.NOTES_ID_BLOOM_FILTER, notesDO.getId().toString());
        //利用rocketMQ异步将笔记保存到ES中
        rocketMQTemplate.asyncSend(RocketMQTopicConstant.NOTES_ADD_ES_TOPIC, JSON.toJSONString(notesDO), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("保存笔记到ES成功");
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("保存笔记到ES失败", throwable);
            }
        });
        // 找到所有的"#话题#"
        List<String> topics = findTopic(notesPublishVO);
        log.info("topics:{}", topics);
        // 查询话题是否存在，不存在则创建
        topics.forEach(topic -> {
            NotesTopicDO notesTopicDO = notesTopicMapper.selectOne(new QueryWrapper<NotesTopicDO>().lambda().eq(NotesTopicDO::getTopicName, topic));
            if (Objects.isNull(notesTopicDO)) {
                notesTopicDO = new NotesTopicDO();
                notesTopicDO.setTopicName(topic);
                notesTopicDO.setCreateUser(notesPublishVO.getBelongUserId());
                notesTopicMapper.insert(notesTopicDO);
                log.info("notesTopicDO:{}", notesTopicDO);
            }
            NotesTopicRelationDO notesTopicRelationDO = new NotesTopicRelationDO();
            notesTopicRelationDO.setNotesId(notesDO.getId());
            notesTopicRelationDO.setTopicId(notesTopicDO.getId());
            notesTopicRelationMapper.insert(notesTopicRelationDO);
        });
        // 找到所有的"@人"，并提取出人的id，发送通知
        List<Long> userIds = findUserId(notesPublishVO);
        log.info("userIds:{}", userIds);
        // TODO 利用rocketMQ异步发送通知
        userIds.forEach(userId -> {
            Map<String, Object> map = new HashMap<>();
            map.put("belongUserId", notesPublishVO.getBelongUserId().toString());
            map.put("toUserId", userId.toString());
            map.put("coverPicture", notesPublishVO.getCoverPicture());
            rocketMQTemplate.asyncSend(RocketMQTopicConstant.NOTES_REMIND_TARGET_TOPIC, JSON.toJSONString(map), new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("发送通知成功");
                }

                @Override
                public void onException(Throwable throwable) {
                    log.error("发送通知失败", throwable);
                }
            });
        });
        // 删除redis中的缓存
        rocketMQTemplate.syncSend(RocketMQTopicConstant.NOTES_REMOVE_REDIS_TOPIC, RedisConstant.REDIS_KEY_NOTES_LAST_PAGE);
        rocketMQTemplate.syncSend(RocketMQTopicConstant.NOTES_REMOVE_REDIS_TOPIC, RedisKey.build(RedisConstant.REDIS_KEY_NOTES_CATEGORY_PAGE, notesDO.getBelongCategory().toString()));
        return ResultUtil.successPost(null);
    }

    @Override
    public Result<NotesPageVO> getLastNotesByPage(Integer page, Integer pageSize) {
        String notesJson = (String) redisCache.get(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_LAST_PAGE, pageSize + "_" + page));
        List<NotesDO> notes;
        if (StringUtils.hasText(notesJson)) {
            notes = JSON.parseArray(notesJson, NotesDO.class);
        } else {
            Integer offset = (page - 1) * pageSize;
            notes = this.baseMapper.selectPageByTime(offset, pageSize);
            redisCache.set(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_LAST_PAGE, pageSize + "_" + page), JSON.toJSONString(notes));
        }
        NotesPageVO notesPageVO = new NotesPageVO();
        buildNotesVo(notesPageVO, notes);
        notesPageVO.setPage(page);
        notesPageVO.setPageSize(pageSize);
        return ResultUtil.successGet(notesPageVO);
    }

    @Override
    public Result<?> initNotesLike(Long notesId) {
        List<UserLikeNotesDO> userLikeNotesList = userLikeNotesMapper.selectList(new QueryWrapper<UserLikeNotesDO>().lambda().eq(UserLikeNotesDO::getNotesId, notesId));
        NotesDO notesDO = this.baseMapper.selectById(notesId);
        if (Objects.isNull(notesDO)) {
            return ResultUtil.successPost(null);
        }
        redisCache.hset(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesId.toString()), "notesLikeNum", notesDO.getNotesLikeNum());
        if (userLikeNotesList.isEmpty()) {
            return ResultUtil.successPost(null);
        }
        // 将所有点赞的用户id存储到redis中，利用redis的set集合去重
        userLikeNotesList.forEach(userLikeNotesDO -> {
            String key = RedisKey.build(RedisConstant.REDIS_KEY_USER_LIKE_NOTES, userLikeNotesDO.getUserId().toString());
            redisCache.addZSet(key, notesId, userLikeNotesDO.getCreateTime().getTime());
        });
        return ResultUtil.successPost(null);
    }

    @Override
    public Result<?> initNotesCollect(Long notesId) {
        List<UserCollectNotesDO> userCollectNotesList = userCollectNotesMapper.selectList(new QueryWrapper<UserCollectNotesDO>().lambda().eq(UserCollectNotesDO::getNotesId, notesId));
        if (userCollectNotesList.isEmpty()) {
            return ResultUtil.successPost(null);
        }
        NotesDO notesDO = this.baseMapper.selectById(notesId);
        if (Objects.isNull(notesDO)) {
            return ResultUtil.successPost(null);
        }
        redisCache.hset(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesId.toString()), "notesCollectionNum", notesDO.getNotesCollectionNum());
        // 将所有收藏的用户id存储到redis中，利用redis的set集合去重
        userCollectNotesList.forEach(userCollectNotesDO -> {
            String key = RedisKey.build(RedisConstant.REDIS_KEY_USER_COLLECT_NOTES, userCollectNotesDO.getUserId().toString());
            redisCache.addZSet(key, notesId, userCollectNotesDO.getCreateTime().getTime());
        });
        return ResultUtil.successPost(null);
    }


    @Override
    public Result<NotesPageVO> getNotesByUserId(Integer page, Integer pageSize, Integer authority, Integer type) {
        NotesPageVO notesPageVO = new NotesPageVO();
        Integer offset = (page - 1) * pageSize;
        String token;
        Long userId = null;
        try {
            token = request.getHeader("token");
            if (StringUtils.hasText(token)) {
                Map<String, Object> map = JWTUtil.parseToken(token);
                userId = (Long) map.get("userId");
            }
        } catch (Exception e) {
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        List<NotesDO> notes = null;
        Integer total = null;
        if (type == 0) {
            notes = this.baseMapper.selectPageByUserId(offset, pageSize, userId, authority);
            total = this.baseMapper.selectCount(new QueryWrapper<NotesDO>().lambda().eq(NotesDO::getBelongUserId, userId).eq(NotesDO::getAuthority, authority));
            // 获取浏览量
            notes.forEach(notesDO -> {
                Object notesViewNum = redisCache.hget(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesDO.getId().toString()), "notesViewNum");
                if (Objects.isNull(notesViewNum)) {
                    notesDO.setNotesViewNum(notesDO.getNotesViewNum());
                    redisCache.hset(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesDO.getId().toString()), "notesViewNum", notesDO.getNotesViewNum());
                } else {
                    notesDO.setNotesViewNum((Integer) notesViewNum);
                }
            });
        } else if (type == 1) {
            List<Long> collect = redisCache.rangeZSet(
                            RedisKey.build(RedisConstant.REDIS_KEY_USER_COLLECT_NOTES, userId.toString()), offset, offset + pageSize)
                    .stream().map(s -> Long.valueOf(s.toString())).collect(Collectors.toList());
            if (collect.isEmpty()) {
                notes = new ArrayList<>();
            } else {
                // 去除空值
                notes = this.baseMapper.selectBatchIds(collect).stream().filter(Objects::nonNull).collect(Collectors.toList());
            }
            total = Math.toIntExact(redisCache.zSetSize(RedisKey.build(RedisConstant.REDIS_KEY_USER_COLLECT_NOTES, userId.toString())));
        } else if (type == 2) {
            List<Long> collect = redisCache.rangeZSet(
                            RedisKey.build(RedisConstant.REDIS_KEY_USER_LIKE_NOTES, userId.toString()), offset, offset + pageSize)
                    .stream().map(s -> Long.valueOf(s.toString())).collect(Collectors.toList());
            if (collect.isEmpty()) {
                notes = new ArrayList<>();
            } else {
                notes = this.baseMapper.selectBatchIds(collect).stream().filter(Objects::nonNull).collect(Collectors.toList());
            }
            total = Math.toIntExact(redisCache.zSetSize(RedisKey.build(RedisConstant.REDIS_KEY_USER_LIKE_NOTES, userId.toString())));
        } else {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);

        }
        String finalToken = token;
        Long finalUserId = userId;
        List<NotesVO> collect = notes.stream().map(notesDO -> {
            NotesVO notesVO = new NotesVO();
            BeanUtils.copyProperties(notesDO, notesVO);
            if (type != 0) {
                notesVO.setNotesViewNum(null);
            }
            Result<?> result = userFeign.getUserInfo(notesDO.getBelongUserId());
            if (result.getCode() == 20010) {
                Map<String, Object> userInfo = (Map<String, Object>) result.getData();
                notesVO.setNickname((String) userInfo.get("nickname"));
                notesVO.setAvatarUrl((String) userInfo.get("avatarUrl"));
            }
            Object notesLikeNum = redisCache.hget(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesDO.getId().toString()), "notesLikeNum");
            if (Objects.isNull(notesLikeNum)) {
                notesVO.setNotesLikeNum(notesDO.getNotesLikeNum());
                redisCache.hset(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesDO.getId().toString()), "notesLikeNum", notesDO.getNotesLikeNum());
            } else {
                notesVO.setNotesLikeNum((Integer) notesLikeNum);
            }
            // 判断当前用户是否点赞
            try {
                if (StringUtils.hasText(finalToken)) {
                    String key = null;
                    if (finalUserId != null) {
                        key = RedisKey.build(RedisConstant.REDIS_KEY_USER_LIKE_NOTES, finalUserId.toString());
                    }
                    Boolean isLike = Objects.nonNull(redisCache.zSetScore(key, notesDO.getId()));
                    notesVO.setIsLike(isLike);
                } else {
                    notesVO.setIsLike(false);
                }
            } catch (Exception e) {
                log.error("获取当前用户id失败", e);
                notesVO.setIsLike(false);
            }
            return notesVO;
        }).collect(Collectors.toList());
        notesPageVO.setList(collect);
        notesPageVO.setTotal(total);
        notesPageVO.setPage(page);
        notesPageVO.setPageSize(pageSize);
        return ResultUtil.successGet(notesPageVO);
    }

    @Override
    public Result<NotesPageVO> getNotesByView(Integer page, Integer pageSize, Integer type, Long userId) {
        NotesPageVO notesPageVO = new NotesPageVO();
        Integer offset = (page - 1) * pageSize;
        List<NotesDO> notes = null;
        if (type == 0) {
            notes = this.baseMapper.selectPageByUserId(offset, pageSize, userId, 0);
        } else if (type == 1) {
            List<Long> collect = redisCache.rangeZSet(
                            RedisKey.build(RedisConstant.REDIS_KEY_USER_COLLECT_NOTES, userId.toString()), offset, offset + pageSize)
                    .stream().map(s -> Long.valueOf(s.toString())).collect(Collectors.toList());
            if (collect.isEmpty()) {
                notes = new ArrayList<>();
            } else {
                // 去除空值
                notes = this.baseMapper.selectBatchIds(collect).stream().filter(Objects::nonNull).collect(Collectors.toList());
            }
        } else if (type == 2) {
            List<Long> collect = redisCache.rangeZSet(
                            RedisKey.build(RedisConstant.REDIS_KEY_USER_LIKE_NOTES, userId.toString()), offset, offset + pageSize)
                    .stream().map(s -> Long.valueOf(s.toString())).collect(Collectors.toList());
            if (collect.isEmpty()) {
                notes = new ArrayList<>();
            } else {
                notes = this.baseMapper.selectBatchIds(collect).stream().filter(Objects::nonNull).collect(Collectors.toList());
            }
        } else {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        buildNotesVo(notesPageVO, notes);
        notesPageVO.setPage(page);
        notesPageVO.setPageSize(pageSize);
        return ResultUtil.successGet(notesPageVO);
    }

    @Override
    public Result<NotesPageVO> getAttentionUserNotes(Integer page, Integer pageSize) {
        String token = request.getHeader("token");
        Long userId;
        try {
            userId = JWTUtil.getCurrentUserId(token);
        } catch (Exception e) {
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        Result<List<Long>> result = userFeign.getAttentionUserId(userId);
        if (result.getCode() != 20010) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        List<Long> attentionUserId = result.getData();
        if (attentionUserId.isEmpty()) {
            return ResultUtil.successGet(new NotesPageVO());
        }
        NotesPageVO notesPageVO = new NotesPageVO();
        Integer offset = (page - 1) * pageSize;
        List<NotesDO> notes = this.baseMapper.selectPageByAttentionUserId(offset, pageSize, attentionUserId);
        buildNotesVo(notesPageVO, notes);
        notesPageVO.setPage(page);
        notesPageVO.setPageSize(pageSize);
        return ResultUtil.successGet(notesPageVO);
    }

    @Override
    public Result<NotesPageVO> getNotesByCategoryId(Integer page, Integer pageSize, Integer categoryId, Integer type, Integer notesType) {
        String notesJson = (String) redisCache.get(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_CATEGORY_PAGE, categoryId + "_" + type + "_" + notesType + "_" + pageSize + "_" + page));
        List<NotesDO> notes = null;
        if (StringUtils.hasText(notesJson)) {
            notes = JSON.parseArray(notesJson, NotesDO.class);
        } else {
            Integer offset = (page - 1) * pageSize;
            if (type == 0) {
                notes = this.baseMapper.selectPageByCategoryIdByUpdateTime(offset, pageSize, categoryId, notesType);
            } else {
                notes = this.baseMapper.selectPageByCategoryIdOrderByPraise(offset, pageSize, categoryId, notesType);
            }
            redisCache.set(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_CATEGORY_PAGE, categoryId + "_" + type + "_" + notesType + "_" + pageSize + "_" + page), JSON.toJSONString(notes));
        }
        NotesPageVO notesPageVO = new NotesPageVO();
        buildNotesVo(notesPageVO, notes);
        notesPageVO.setPage(page);
        notesPageVO.setPageSize(pageSize);
        return ResultUtil.successGet(notesPageVO);
    }

    private void buildNotesVo(NotesPageVO notesPageVO, List<NotesDO> notes) {
        List<NotesVO> collect = notes.stream().map(notesDO -> {
            NotesVO notesVO = new NotesVO();
            BeanUtils.copyProperties(notesDO, notesVO);
            Result<?> result = userFeign.getUserInfo(notesDO.getBelongUserId());
            if (result.getCode() == 20010) {
                Map<String, Object> userInfo = (Map<String, Object>) result.getData();
                notesVO.setNickname((String) userInfo.get("nickname"));
                notesVO.setAvatarUrl((String) userInfo.get("avatarUrl"));
            }
            Object notesLikeNum = redisCache.hget(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesDO.getId().toString()), "notesLikeNum");
            if (Objects.isNull(notesLikeNum)) {
                notesVO.setNotesLikeNum(notesDO.getNotesLikeNum());
                redisCache.hset(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesDO.getId().toString()), "notesLikeNum", notesDO.getNotesLikeNum());
            } else {
                notesVO.setNotesLikeNum((Integer) notesLikeNum);
            }
            Object notesCollectionNum = redisCache.hget(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesDO.getId().toString()), "notesCollectionNum");
            if (Objects.isNull(notesCollectionNum)) {
                notesVO.setNotesCollectNum(notesDO.getNotesCollectionNum());
                redisCache.hset(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesDO.getId().toString()), "notesCollectionNum", notesDO.getNotesCollectionNum());
            } else {
                notesVO.setNotesCollectNum((Integer) notesCollectionNum);
            }
            String token = request.getHeader("token");
            try {
                Long currentUserId = JWTUtil.getCurrentUserId(token);
                // 判断当前用户是否点赞
                String key = RedisKey.build(RedisConstant.REDIS_KEY_USER_LIKE_NOTES, currentUserId.toString());
                Boolean isLike = Objects.nonNull(redisCache.zSetScore(key, notesDO.getId()));
                notesVO.setIsLike(isLike);
                // 判断当前用户是否收藏
                String key1 = RedisKey.build(RedisConstant.REDIS_KEY_USER_COLLECT_NOTES, currentUserId.toString());
                Boolean isCollect = Objects.nonNull(redisCache.zSetScore(key1, notesDO.getId()));
                notesVO.setIsCollect(isCollect);
            } catch (Exception e) {
                log.error("获取当前用户id失败", e);
                notesVO.setIsLike(false);
                notesVO.setIsCollect(false);
            }
            return notesVO;
        }).collect(Collectors.toList());
        notesPageVO.setList(collect);
    }

    @Override
    public Result<NotesVO> getNotesByNotesId(Long notesId) {
        NotesDO notesDO = this.baseMapper.selectById(notesId);
        if (Objects.isNull(notesDO)) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        NotesVO notesVO = new NotesVO();
        BeanUtils.copyProperties(notesDO, notesVO);
        String notesResources = notesDO.getNotesResources();
        List<String> resources = JSON.parseObject(notesResources, List.class);
        List<ResourcesDTO> collect = resources.stream().map(resource -> {
            ResourcesDTO resourcesDTO = new ResourcesDTO();
            resourcesDTO.setUrl(resource);
            return resourcesDTO;
        }).collect(Collectors.toList());
        notesVO.setNotesResources(collect);
        Result<?> result = userFeign.getUserInfo(notesDO.getBelongUserId());
        if (result.getCode() == 20010) {
            Map<String, Object> userInfo = (Map<String, Object>) result.getData();
            notesVO.setNickname((String) userInfo.get("nickname"));
            notesVO.setAvatarUrl((String) userInfo.get("avatarUrl"));
        }
        Object notesLikeNum = redisCache.hget(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesDO.getId().toString()), "notesLikeNum");
        if (Objects.isNull(notesLikeNum)) {
            notesVO.setNotesLikeNum(notesDO.getNotesLikeNum());
            redisCache.hset(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesDO.getId().toString()), "notesLikeNum", notesDO.getNotesLikeNum());
        } else {
            notesVO.setNotesLikeNum((Integer) notesLikeNum);
        }
        Object notesCollectionNum = redisCache.hget(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesDO.getId().toString()), "notesCollectionNum");
        if (Objects.isNull(notesCollectionNum)) {
            notesVO.setNotesCollectNum(notesDO.getNotesCollectionNum());
            redisCache.hset(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesDO.getId().toString()), "notesCollectionNum", notesDO.getNotesCollectionNum());
        } else {
            notesVO.setNotesCollectNum((Integer) notesCollectionNum);
        }
        String token = request.getHeader("token");
        Long userId = null;
        try {
            userId = JWTUtil.getCurrentUserId(token);
        } catch (Exception e) {
            log.error("获取当前用户id失败", e);
            notesVO.setIsLike(false);
            notesVO.setIsCollect(false);
        }
        // 判断当前用户是否收藏
        if (StringUtils.hasText(token) && Objects.nonNull(userId)) {
            String key = RedisKey.build(RedisConstant.REDIS_KEY_USER_COLLECT_NOTES, userId.toString());
            Boolean isCollect = Objects.nonNull(redisCache.zSetScore(key, notesId));
            notesVO.setIsCollect(isCollect);
        } else {
            notesVO.setIsCollect(false);
        }
        // 判断当前用户是否点赞
        if (StringUtils.hasText(token) && Objects.nonNull(userId)) {
            String key = RedisKey.build(RedisConstant.REDIS_KEY_USER_LIKE_NOTES, userId.toString());
            Boolean isLike = Objects.nonNull(redisCache.zSetScore(key, notesId));
            notesVO.setIsLike(isLike);
        } else {
            notesVO.setIsLike(false);
        }
        // 判断笔记所属用户与当前用户是否为一个人
        if (Objects.nonNull(userId)) {
            if (userId.equals(notesDO.getBelongUserId())) {
                notesVO.setIsFollow(true);
            } else {
                // 判断当前用户是否关注笔记所属用户
                Result<Boolean> result1 = userFeign.selectOneByUserIdAndAttentionIdIsExist(userId, notesDO.getBelongUserId());
                if (result1.getCode() == 20010) {
                    notesVO.setIsFollow(result1.getData());
                } else {
                    notesVO.setIsFollow(false);
                }
            }
        } else {
            notesVO.setIsFollow(false);
        }
        return ResultUtil.successGet(notesVO);
    }

    @Override
    public Result<?> praiseNotes(Long notesId, Long userId, Long targetUserId) {
        NotesDO notesDO = this.baseMapper.selectById(notesId);
        if (Objects.isNull(notesDO)) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        String key = RedisKey.build(RedisConstant.REDIS_KEY_USER_LIKE_NOTES, userId.toString());
        boolean isLike = Objects.nonNull(redisCache.zSetScore(key, notesId));
        Map<String, Object> userLikeNotesMap = new HashMap<>();
        userLikeNotesMap.put("userId", userId);
        userLikeNotesMap.put("notesId", notesId);
        userLikeNotesMap.put("isLike", !isLike);
        int i = CodeUtil.hashIndex(userId);
        // 便于定时任务更新数据库，不能直接删除键，避免不能删除数据库中的点赞记录，定时任务判断isLike字段操作数据库，如果为false则删除
        String userLikeNotesKey = RedisKey.build(RedisConstant.REDIS_KEY_USER_LIKE_NOTES_RECENT, i + "");
        // 先判断有记录没有，若与之前相反，则删除之前的记录，否则直接新增覆盖
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("notesId", notesId);
        map.put("isLike", isLike);
        Double v = redisCache.zSetScore(userLikeNotesKey, JSON.toJSONString(map));
        if (Objects.nonNull(v)) {
            redisCache.removeZSet(userLikeNotesKey, JSON.toJSONString(map));
        }
        redisCache.addZSet(userLikeNotesKey, JSON.toJSONString(userLikeNotesMap), System.currentTimeMillis());
        if (redisCache.hget(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesId.toString()), "notesLikeNum") == null) {
            redisCache.hset(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesId.toString()), "notesLikeNum", notesDO.getNotesLikeNum());
        }
        // 根据用户维度存储点赞记录
        if (isLike) {
            redisCache.removeZSet(key, notesId);
            redisCache.hincr(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesId.toString()), "notesLikeNum", -1);
        } else {
            redisCache.addZSet(key, notesId, System.currentTimeMillis());
            redisCache.hincr(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesId.toString()), "notesLikeNum", 1);
        }
        // TODO 利用rocketMQ异步给发送targetUserId通知
        if (isLike) {
            return ResultUtil.successPost(null);
        }
        Map<String, Object> messageMap = new HashMap<>();
        String nickname = (String) redisCache.hget(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, userId.toString()), "nickname");
        String avatarUrl = (String) redisCache.hget(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, userId.toString()), "avatarUrl");
        if (!StringUtils.hasText(avatarUrl) || !StringUtils.hasText(nickname)) {
            Result<?> result = userFeign.getUserInfo(userId);
            if (result.getCode() == 20010) {
                Map<String, Object> userInfo = (Map<String, Object>) result.getData();
                if (!StringUtils.hasText(nickname)) {
                    nickname = (String) userInfo.get("nickname");
                }
                if (!StringUtils.hasText(avatarUrl)) {
                    avatarUrl = (String) userInfo.get("avatarUrl");
                }
            }
        }
        Map<String, String> contentMap = new HashMap<>();
        contentMap.put("text", "点赞了你的笔记");
        contentMap.put("notesId", notesId.toString());
        contentMap.put("notesType", notesDO.getNotesType().toString());
        contentMap.put("notesCoverPicture", notesDO.getCoverPicture());
        messageMap.put("from", userId);
        messageMap.put("fromName", nickname);
        messageMap.put("fromAvatar", avatarUrl);
        messageMap.put("to", targetUserId);
        messageMap.put("time", System.currentTimeMillis());
        messageMap.put("messageType", 8);
        messageMap.put("chatType", 0);
        messageMap.put("friendType", 1);
        messageMap.put("content", JSON.toJSONString(contentMap));
        rocketMQTemplate.asyncSend(RocketMQTopicConstant.PRAISE_AND_COLLECT_REMIND_TOPIC, JSON.toJSONString(messageMap), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("发送点赞通知成功");
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("发送点赞通知失败", throwable);
            }
        });
        return ResultUtil.successPost(null);
    }

    @Override
    public Result<?> collectNotes(Long notesId, Long userId, Long targetUserId) {
        NotesDO notesDO = this.baseMapper.selectById(notesId);
        if (Objects.isNull(notesDO)) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        String key = RedisKey.build(RedisConstant.REDIS_KEY_USER_COLLECT_NOTES, userId.toString());
        boolean isCollect = Objects.nonNull(redisCache.zSetScore(key, notesId));
        Map<String, Object> userCollectNotesMap = new HashMap<>();
        userCollectNotesMap.put("userId", userId);
        userCollectNotesMap.put("notesId", notesId);
        userCollectNotesMap.put("isCollect", !isCollect);
        // 考虑到由于新增的收藏记录可能过多，分片存储，避免大key，分16片
        int i = CodeUtil.hashIndex(userId);
        // 便于定时任务更新数据库，不能直接删除键，避免不能删除数据库中的收藏记录，定时任务判断isCollect字段操作数据库，如果为false则删除
        String userCollectNotesKey = RedisKey.build(RedisConstant.REDIS_KEY_USER_COLLECT_NOTES_RECENT, i + "");
        // 先判断有记录没有，若与之前相反，则删除之前的记录，否则直接新增覆盖
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("notesId", notesId);
        map.put("isCollect", isCollect);
        Double v = redisCache.zSetScore(userCollectNotesKey, JSON.toJSONString(map));
        if (Objects.nonNull(v)) {
            redisCache.removeZSet(userCollectNotesKey, JSON.toJSONString(map));
        }
        redisCache.addZSet(userCollectNotesKey, JSON.toJSONString(userCollectNotesMap), System.currentTimeMillis());
        if (redisCache.hget(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesId.toString()), "notesCollectionNum") == null) {
            redisCache.hset(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesId.toString()), "notesCollectionNum", notesDO.getNotesCollectionNum());
        }
        // 根据用户维度存储收藏记录
        if (isCollect) {
            redisCache.removeZSet(key, notesId);
            redisCache.hincr(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesId.toString()), "notesCollectionNum", -1);
        } else {
            redisCache.addZSet(key, notesId, System.currentTimeMillis());
            redisCache.hincr(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesId.toString()), "notesCollectionNum", 1);
        }
        // TODO 利用rocketMQ异步给发送targetUserId通知
        if (isCollect) {
            return ResultUtil.successPost(null);
        }
        Map<String, Object> messageMap = new HashMap<>();
        String nickname = (String) redisCache.hget(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, userId.toString()), "nickname");
        String avatarUrl = (String) redisCache.hget(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, userId.toString()), "avatarUrl");
        Map<String, String> contentMap = new HashMap<>();
        if (!StringUtils.hasText(avatarUrl) || !StringUtils.hasText(nickname)) {
            Result<?> result = userFeign.getUserInfo(userId);
            if (result.getCode() == 20010) {
                Map<String, Object> userInfo = (Map<String, Object>) result.getData();
                if (!StringUtils.hasText(nickname)) {
                    nickname = (String) userInfo.get("nickname");
                }
                if (!StringUtils.hasText(avatarUrl)) {
                    avatarUrl = (String) userInfo.get("avatarUrl");
                }
            }
        }
        contentMap.put("text", "收藏了你的笔记");
        contentMap.put("notesId", notesId.toString());
        contentMap.put("notesType", notesDO.getNotesType().toString());
        contentMap.put("notesCoverPicture", notesDO.getCoverPicture());
        messageMap.put("from", userId);
        messageMap.put("fromName", nickname);
        messageMap.put("fromAvatar", avatarUrl);
        messageMap.put("to", targetUserId);
        messageMap.put("time", System.currentTimeMillis());
        messageMap.put("messageType", 8);
        messageMap.put("chatType", 0);
        messageMap.put("friendType", 1);
        messageMap.put("content", JSON.toJSONString(contentMap));
        rocketMQTemplate.asyncSend(RocketMQTopicConstant.PRAISE_AND_COLLECT_REMIND_TOPIC, JSON.toJSONString(messageMap), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("发送收藏通知成功");
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("发送收藏通知失败", throwable);
            }
        });
        return ResultUtil.successPost(null);
    }

    @Override
    public Result<?> viewNotes(Long notesId) {
        if (redisCache.hget(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesId.toString()), "notesViewNum") == null) {
            NotesDO notesDO = this.getById(notesId);
            if (Objects.isNull(notesDO)) {
                throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
            }
            redisCache.hset(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesId.toString()), "notesViewNum", notesDO.getNotesViewNum());
        }
        redisCache.hincr(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesId.toString()), "notesViewNum", 1);
        return ResultUtil.successPost(null);
    }

    @Override
    public Result<?> updateNotes(NotesPublishVO notesPublishVO) {
        NotesDO notesDO = this.getById(notesPublishVO.getNotesId());
        if (Objects.isNull(notesDO)) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if (!notesDO.getBelongUserId().equals(notesPublishVO.getBelongUserId())) {
            throw new BusinessException(ExceptionMsgEnum.NO_PERMISSION);
        }
        // 获取当前用户id
        String token = request.getHeader("token");
        Long currentUserId = null;
        try {
            if (StringUtils.hasText(token)) {
                currentUserId = JWTUtil.getCurrentUserId(token);
            }
        } catch (Exception e) {
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        if (!notesDO.getBelongUserId().equals(currentUserId)) {
            throw new BusinessException(ExceptionMsgEnum.NO_PERMISSION);
        }
        List<String> notesResources = JSON.parseObject(notesPublishVO.getNotesResources(), List.class);
        notesParameterCheck(notesPublishVO, notesResources);
        // 更新不一样的字段
        if (StringUtils.hasText(notesPublishVO.getTitle())) {
            notesDO.setTitle(notesPublishVO.getTitle());
        }
        if (StringUtils.hasText(notesPublishVO.getContent())) {
            notesDO.setContent(notesPublishVO.getContent());
        }
        if (notesPublishVO.getNotesType() == 0) {
            notesDO.setCoverPicture(notesResources.get(0));
        } else {
            if (!StringUtils.hasText(notesPublishVO.getCoverPicture())) {
                notesDO.setCoverPicture(notesResources.get(0) + "?x-oss-process=video/snapshot,t_0,f_jpg,w_0,h_0,m_fast");
            } else {
                notesDO.setCoverPicture(notesPublishVO.getCoverPicture());
            }
        }
        notesDO.setNotesResources(notesPublishVO.getNotesResources());
        notesDO.setNotesType(notesPublishVO.getNotesType());
        // 设置省份
        try {
            String p = AddressUtil.getAddress(notesPublishVO.getLongitude(), notesPublishVO.getLatitude());
            notesDO.setProvince(p);
        } catch (Exception e) {
            log.error("获取省份失败", e);
            notesDO.setProvince("未知");
        }
        notesDO.setUpdateTime(new Date());
        this.updateById(notesDO);
        //利用rocketMQ异步将笔记更新到ES中
        rocketMQTemplate.asyncSend(RocketMQTopicConstant.NOTES_UPDATE_ES_TOPIC, JSON.toJSONString(notesDO), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("更新笔记到ES成功");
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("更新笔记到ES失败", throwable);
            }
        });
        // 找到所有的"#话题#"
        List<String> topics = findTopic(notesPublishVO);
        log.info("topics:{}", topics);
        // 查询话题是否存在，不存在则创建
        topics.forEach(topic -> {
            NotesTopicDO notesTopicDO = notesTopicMapper.selectOne(new QueryWrapper<NotesTopicDO>().lambda().eq(NotesTopicDO::getTopicName, topic));
            if (Objects.isNull(notesTopicDO)) {
                notesTopicDO = new NotesTopicDO();
                notesTopicDO.setTopicName(topic);
                notesTopicDO.setCreateUser(notesPublishVO.getBelongUserId());
                notesTopicMapper.insert(notesTopicDO);
                log.info("notesTopicDO:{}", notesTopicDO);
            }
            NotesTopicRelationDO notesTopicRelationDO = new NotesTopicRelationDO();
            notesTopicRelationDO.setNotesId(notesDO.getId());
            notesTopicRelationDO.setTopicId(notesTopicDO.getId());
            notesTopicRelationMapper.insert(notesTopicRelationDO);
        });
        // 解除之前的关联关系
        notesTopicRelationMapper.delete(new QueryWrapper<NotesTopicRelationDO>().lambda().eq(NotesTopicRelationDO::getNotesId, notesDO.getId()));
        // 找到所以@人
        List<Long> userIds = findUserId(notesPublishVO);
        log.info("userIds:{}", userIds);
        // TODO 利用rocketMQ异步发送通知
        userIds.forEach(userId -> {
            Map<String, Object> map = new HashMap<>();
            map.put("belongUserId", notesPublishVO.getBelongUserId().toString());
            map.put("toUserId", userId.toString());
            map.put("coverPicture", notesPublishVO.getCoverPicture());
            rocketMQTemplate.asyncSend(RocketMQTopicConstant.NOTES_REMIND_TARGET_TOPIC, JSON.toJSONString(map), new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("发送通知成功");
                }

                @Override
                public void onException(Throwable throwable) {
                    log.error("发送通知失败", throwable);
                }
            });
        });
        // 删除redis缓存
        rocketMQTemplate.syncSend(RocketMQTopicConstant.NOTES_REMOVE_REDIS_TOPIC, RedisConstant.REDIS_KEY_NOTES_LAST_PAGE);
        rocketMQTemplate.syncSend(RocketMQTopicConstant.NOTES_REMOVE_REDIS_TOPIC, RedisKey.build(RedisConstant.REDIS_KEY_NOTES_CATEGORY_PAGE, notesDO.getBelongCategory().toString()));
        return ResultUtil.successPut(null);
    }

    @Override
    public Result<?> deleteNotes(Long notesId) {
        NotesDO notesDO = this.baseMapper.selectById(notesId);
        if (Objects.isNull(notesDO)) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        // 判断当前用户是否有权限删除
        String token = request.getHeader("token");
        Long currentUserId = null;
        try {
            if (StringUtils.hasText(token)) {
                currentUserId = JWTUtil.getCurrentUserId(token);
            }
        } catch (Exception e) {
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        if (!notesDO.getBelongUserId().equals(currentUserId)) {
            throw new BusinessException(ExceptionMsgEnum.NO_PERMISSION);
        }
        this.baseMapper.deleteById(notesId);
        // 删除笔记的点赞，收藏，浏览量
        redisCache.del(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesId.toString()));
        // 删除笔记的评论
        rocketMQTemplate.asyncSend(RocketMQTopicConstant.NOTES_DELETE_COMMENT_TOPIC, notesId.toString(), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("删除笔记的评论成功");
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("删除笔记的评论失败", throwable);
            }
        });
        //利用rocketMQ异步将笔记从ES中删除
        rocketMQTemplate.asyncSend(RocketMQTopicConstant.NOTES_DELETE_ES_TOPIC, notesId.toString(), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("删除笔记从ES成功");
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("删除笔记从ES失败", throwable);
            }
        });
        // 删除redis缓存
        rocketMQTemplate.syncSend(RocketMQTopicConstant.NOTES_REMOVE_REDIS_TOPIC, RedisConstant.REDIS_KEY_NOTES_LAST_PAGE);
        rocketMQTemplate.syncSend(RocketMQTopicConstant.NOTES_REMOVE_REDIS_TOPIC, RedisKey.build(RedisConstant.REDIS_KEY_NOTES_CATEGORY_PAGE, notesDO.getBelongCategory().toString()));
        return ResultUtil.successDelete(null);
    }

    @Override
    public Result<?> changeNotesAuthority(Long notesId, Integer authority) {
        NotesDO notesDO = this.baseMapper.selectById(notesId);
        if (Objects.isNull(notesDO)) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        // 判断当前用户是否有权限修改
        String token = request.getHeader("token");
        Long currentUserId = null;
        try {
            if (StringUtils.hasText(token)) {
                currentUserId = JWTUtil.getCurrentUserId(token);
            }
        } catch (Exception e) {
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        if (!notesDO.getBelongUserId().equals(currentUserId)) {
            throw new BusinessException(ExceptionMsgEnum.NO_PERMISSION);
        }
        notesDO.setAuthority(authority);
        this.updateById(notesDO);
        //利用rocketMQ异步将笔记更新到ES中
        rocketMQTemplate.asyncSend(RocketMQTopicConstant.NOTES_UPDATE_ES_TOPIC, JSON.toJSONString(notesDO), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("更新笔记到ES成功");
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("更新笔记到ES失败", throwable);
            }
        });
        // 删除redis缓存
        rocketMQTemplate.syncSend(RocketMQTopicConstant.NOTES_REMOVE_REDIS_TOPIC, RedisConstant.REDIS_KEY_NOTES_LAST_PAGE);
        rocketMQTemplate.syncSend(RocketMQTopicConstant.NOTES_REMOVE_REDIS_TOPIC, RedisKey.build(RedisConstant.REDIS_KEY_NOTES_CATEGORY_PAGE, notesDO.getBelongCategory().toString()));
        return ResultUtil.successPost(null);
    }

    @Override
    public Result<Map<String, Integer>> getAllNotesCountAndPraiseCountAndCollectCount() {
        String token = request.getHeader("token");
        Long userId = null;
        try {
            if (StringUtils.hasText(token)) {
                Map<String, Object> map = JWTUtil.parseToken(token);
                userId = (Long) map.get("userId");
            }
        } catch (Exception e) {
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        Map<String, Integer> map = new HashMap<>();
        Integer notesCount = this.baseMapper.selectCount(new QueryWrapper<NotesDO>().lambda().eq(NotesDO::getBelongUserId, userId));
        // 获取自己发布的所有笔记的点赞数
        Integer praiseCount = this.baseMapper.getPraiseCountByUserId(userId);
        // 获取自己发布的所有笔记的收藏数
        Integer collectCount = this.baseMapper.getCollectCountByUserId(userId);
        map.put("notesCount", notesCount);
        map.put("praiseCount", praiseCount);
        map.put("collectCount", collectCount);
        return ResultUtil.successGet(map);
    }

    private List<Long> findUserId(NotesPublishVO notesPublishVO) {
        List<Long> userIds = new ArrayList<>();
        int isFlutter;
        String s1 = request.getHeader("isFlutter");
        if (StringUtils.hasText(s1)) {
            try {
                isFlutter = Integer.parseInt(s1);
            } catch (NumberFormatException e) {
                isFlutter = 0;
            }
        } else {
            isFlutter = 0;
        }
        if (isFlutter == 0) {
            Document document = Jsoup.parse(notesPublishVO.getContent());
            Elements elements = document.select("a");
            for (Element element : elements) {
                String href = element.attr("href");
                if (href.contains("userId")) {
                    String userId = href.substring(href.indexOf("userId") + 9, href.indexOf("}") - 1);
                    userIds.add(Long.valueOf(userId));
                }
            }
        } else {
            // 去除\u200B，再找到所有的    // Map<String, String> map = {
            //    //   "id": "123456",
            //    //   "name": "somebody",
            //    // };
            //    // String realText='@${jsonEncode(map)} ';
            String content = notesPublishVO.getContent().replace("\u200B", "");
            // 找到所有的"@用户 "
            Pattern pattern = Pattern.compile("@[^@]+ ");
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                String group = matcher.group();
                String jsonUserId = group.substring(1, group.length() - 1);
                try {
                    String userId = JSON.parseObject(jsonUserId).getString("id");
                    userIds.add(Long.valueOf(userId));
                } catch (Exception e) {
                    log.info("解析json失败", e);
                }
            }
        }
        return userIds;
    }

    private List<String> findTopic(NotesPublishVO notesPublishVO) {
        List<String> topics = new ArrayList<>();
        int isFlutter;
        String s1 = request.getHeader("isFlutter");
        if (StringUtils.hasText(s1)) {
            try {
                isFlutter = Integer.parseInt(s1);
            } catch (NumberFormatException e) {
                isFlutter = 0;
            }
        } else {
            isFlutter = 0;
        }
        if (isFlutter == 0) {
            Document document = Jsoup.parse(notesPublishVO.getContent());
            Elements elements = document.select("a");
            for (Element element : elements) {
                String href = element.attr("href");
                if (href.contains("topicname")) {
                    String topicName = href.substring(href.indexOf("topicname") + 12, href.indexOf("}") - 1);
                    topics.add(topicName);
                }
            }
        } else {
            // 去除\u200B，再找到所有的"#话题 "
            Pattern pattern = Pattern.compile("#[^#]+ ");
            String content = notesPublishVO.getContent().replace("\u200B", "");
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                String group = matcher.group();
                topics.add(group.substring(1, group.length() - 1));
            }
        }
        return topics;
    }

    private void notesParameterCheck(NotesPublishVO notesPublishVO, List<String> notesResources) {
        if (notesResources.isEmpty()) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if (notesPublishVO.getNotesType() == 0 && notesResources.size() > 9) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if (notesPublishVO.getNotesType() == 1 && notesResources.size() > 1) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if (!StringUtils.hasText(notesPublishVO.getTitle())) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
    }
}
