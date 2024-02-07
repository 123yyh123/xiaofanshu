package com.yyh.xfs.notes.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.redis.constant.RedisConstant;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.common.redis.utils.RedisKey;
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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
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

    public NotesServiceImpl(NotesTopicMapper notesTopicMapper,
                            NotesTopicRelationMapper notesTopicRelationMapper,
                            NotesCategoryMapper notesCategoryMapper,
                            RocketMQTemplate rocketMQTemplate,
                            UserFeign userFeign,
                            UserLikeNotesMapper userLikeNotesMapper,
                            UserCollectNotesMapper userCollectNotesMapper,
                            RedisCache redisCache,
                            HttpServletRequest request) {
        this.notesTopicMapper = notesTopicMapper;
        this.notesTopicRelationMapper = notesTopicRelationMapper;
        this.notesCategoryMapper = notesCategoryMapper;
        this.rocketMQTemplate = rocketMQTemplate;
        this.userFeign = userFeign;
        this.userLikeNotesMapper = userLikeNotesMapper;
        this.userCollectNotesMapper = userCollectNotesMapper;
        this.redisCache = redisCache;
        this.request = request;
    }

    @Override
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
        // 利用百度AI接口进行文章分类
        String category = NotesUtils.createCategory(notesPublishVO.getTitle(), notesPublishVO.getRealContent());
        log.info("category:{}", category);
        NotesCategoryDO notesCategoryDO = notesCategoryMapper.selectOne(new QueryWrapper<NotesCategoryDO>().lambda().eq(NotesCategoryDO::getCategoryName, category));
        if (Objects.isNull(notesCategoryDO)) {
            // 默认分类
            notesDO.setBelongCategory(27);
        } else {
            notesDO.setBelongCategory(notesCategoryDO.getId());
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
        //利用rocketMQ异步将笔记保存到ES中
        rocketMQTemplate.asyncSend("notes-add-es-topic", JSON.toJSONString(notesDO), new SendCallback() {
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
            rocketMQTemplate.asyncSend("notes-remind-target-topic", JSON.toJSONString(map), new SendCallback() {
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
        return ResultUtil.successPost(null);
    }

    @Override
    public Result<NotesPageVO> getLastNotesByPage(Integer page, Integer pageSize) {
        NotesPageVO notesPageVO = new NotesPageVO();
        Integer offset = (page - 1) * pageSize;
        List<NotesDO> notes = this.baseMapper.selectPageByTime(offset, pageSize);
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
            // 判断当前用户是否点赞
            String token = request.getHeader("token");
            try {
                if (StringUtils.hasText(token)) {
                    Map<String, Object> map = JWTUtil.parseToken(token);
                    Long userId = (Long) map.get("userId");
                    String key = RedisKey.build(RedisConstant.REDIS_KEY_USER_LIKE_NOTES, userId.toString());
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
        notesPageVO.setTotal(this.baseMapper.selectCount(null));
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
        } else if (type == 1) {
            notes = this.baseMapper.selectPageByUserIdAndLike(offset, pageSize, userId);
            total = userLikeNotesMapper.selectCount(new QueryWrapper<UserLikeNotesDO>().lambda().eq(UserLikeNotesDO::getUserId, userId));
        } else if (type == 2) {
            notes = this.baseMapper.selectPageByUserIdAndCollect(offset, pageSize, userId);
            total = userCollectNotesMapper.selectCount(new QueryWrapper<UserCollectNotesDO>().lambda().eq(UserCollectNotesDO::getUserId, userId));
        } else {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);

        }
        String finalToken = token;
        Long finalUserId = userId;
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
            if (StringUtils.hasText(token)) {
                Map<String, Object> map = JWTUtil.parseToken(token);
                userId = (Long) map.get("userId");
            }
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
    public Result<?> praiseNotes(Long notesId, Long userId) {
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
        userLikeNotesMap.put("createTime", new Date());
        // 考虑到由于新增的点赞记录可能过多，分片存储，避免大key，分10片
        int i = userLikeNotesMap.hashCode() % 10;
        // 根据用户维度存储点赞记录
        if (isLike) {
            redisCache.removeZSet(key, notesId);
            redisCache.hincr(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesId.toString()), "notesLikeNum", -1);
            // TODO 便于定时任务更新数据库，不能直接删除键，避免不能删除数据库中的点赞记录，定时任务判断isLike字段操作数据库，如果为false则删除
            String userLikeNotesKey = RedisKey.build(RedisConstant.REDIS_KEY_USER_LIKE_NOTES_RECENT, i + "");
            redisCache.sSet(userLikeNotesKey, JSON.toJSONString(userLikeNotesMap));
        } else {
            redisCache.addZSet(key, notesId, System.currentTimeMillis());
            redisCache.hincr(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesId.toString()), "notesLikeNum", 1);
            String userLikeNotesKey = RedisKey.build(RedisConstant.REDIS_KEY_USER_LIKE_NOTES_RECENT, i + "");
            redisCache.sSet(userLikeNotesKey, JSON.toJSONString(userLikeNotesMap));
        }
        return ResultUtil.successPost(null);
    }

    @Override
    public Result<?> collectNotes(Long notesId, Long userId) {
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
        userCollectNotesMap.put("createTime", new Date());
        // 考虑到由于新增的收藏记录可能过多，分片存储，避免大key，分10片
        int i = userCollectNotesMap.hashCode() % 10;
        // 根据用户维度存储收藏记录
        if (isCollect) {
            redisCache.removeZSet(key, notesId);
            redisCache.hincr(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesId.toString()), "notesCollectionNum", -1);
            // TODO 便于定时任务更新数据库，不能直接删除键，避免不能删除数据库中的收藏记录，定时任务判断isCollect字段操作数据库，如果为false则删除
            String userCollectNotesKey = RedisKey.build(RedisConstant.REDIS_KEY_USER_COLLECT_NOTES_RECENT, i + "");
            redisCache.sSet(userCollectNotesKey, JSON.toJSONString(userCollectNotesMap));
        } else {
            redisCache.addZSet(key, notesId, System.currentTimeMillis());
            redisCache.hincr(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, notesId.toString()), "notesCollectionNum", 1);
            String userCollectNotesKey = RedisKey.build(RedisConstant.REDIS_KEY_USER_COLLECT_NOTES_RECENT, i + "");
            redisCache.sSet(userCollectNotesKey, JSON.toJSONString(userCollectNotesMap));
        }
        return ResultUtil.successPost(null);
    }

    private List<Long> findUserId(NotesPublishVO notesPublishVO) {
        List<Long> userIds = new ArrayList<>();
        Document document = Jsoup.parse(notesPublishVO.getContent());
        Elements elements = document.select("a");
        for (Element element : elements) {
            String href = element.attr("href");
            if (href.contains("userId")) {
                String userId = href.substring(href.indexOf("userId") + 9, href.indexOf("}") - 1);
                userIds.add(Long.valueOf(userId));
            }
        }
        return userIds;
    }

    private List<String> findTopic(NotesPublishVO notesPublishVO) {
        List<String> topics = new ArrayList<>();
        Document document = Jsoup.parse(notesPublishVO.getContent());
        Elements elements = document.select("a");
        for (Element element : elements) {
            String href = element.attr("href");
            if (href.contains("topicname")) {
                String topicName = href.substring(href.indexOf("topicname") + 12, href.indexOf("}") - 1);
                topics.add(topicName);
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
