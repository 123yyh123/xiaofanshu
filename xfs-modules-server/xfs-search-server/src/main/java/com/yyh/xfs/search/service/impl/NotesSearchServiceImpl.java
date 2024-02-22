package com.yyh.xfs.search.service.impl;

import com.yyh.xfs.common.domain.PageParam;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.redis.constant.RedisConstant;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.common.redis.utils.RedisKey;
import com.yyh.xfs.common.utils.HtmlParseUtils;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.common.web.exception.BusinessException;
import com.yyh.xfs.common.web.utils.JWTUtil;
import com.yyh.xfs.notes.domain.NotesDO;
import com.yyh.xfs.notes.vo.NotesPageVO;
import com.yyh.xfs.notes.vo.NotesVO;
import com.yyh.xfs.search.domain.NotesEsDO;
import com.yyh.xfs.search.feign.UserFeign;
import com.yyh.xfs.search.service.NotesSearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.sort.*;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.util.AnnotatedTypeScanner;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author yyh
 * @date 2024-01-24
 */
@Service
@Slf4j
public class NotesSearchServiceImpl implements NotesSearchService {
    private final UserFeign userFeign;
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;
    private final RedisCache redisCache;
    private final HttpServletRequest request;

    public NotesSearchServiceImpl(ElasticsearchRestTemplate elasticsearchRestTemplate,
                                  UserFeign userFeign,
                                  RedisCache redisCache,
                                  HttpServletRequest request) {
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
        this.userFeign = userFeign;
        this.redisCache = redisCache;
        this.request = request;
    }

    /**
     * 添加笔记
     *
     * @param notesDO 笔记
     */
    @Override
    public void addNotes(NotesDO notesDO) {
        NotesEsDO notesEsDO = new NotesEsDO();
        BeanUtils.copyProperties(notesDO, notesEsDO);
        String s = HtmlParseUtils.htmlToText(notesEsDO.getContent());
        notesEsDO.setTextContent(s);
        Date createTime = notesDO.getCreateTime();
        notesEsDO.setCreateTime(createTime.getTime());
        Date updateTime = notesDO.getUpdateTime();
        notesEsDO.setUpdateTime(updateTime.getTime());
        GeoPoint geoPoint = new GeoPoint(notesDO.getLatitude(), notesDO.getLongitude());
        notesEsDO.setGeoPoint(geoPoint);
        elasticsearchRestTemplate.save(notesEsDO);
    }

    /**
     * 获取附近的笔记
     *
     * @param pageParam 分页参数
     * @return 笔记列表
     */
    @Override
    public Result<NotesPageVO> getNotesNearBy(PageParam pageParam) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        // 设置分页
        PageRequest pageRequest = PageRequest.of(pageParam.getPage() - 1, pageParam.getPageSize());
        nativeSearchQueryBuilder.withPageable(pageRequest);
        // 设置查询条件，默认距离100km的笔记，authority为0,表示公开
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        GeoDistanceQueryBuilder geoDistanceQueryBuilder = new GeoDistanceQueryBuilder("geoPoint");
        geoDistanceQueryBuilder.point(pageParam.getLatitude(), pageParam.getLongitude());
        geoDistanceQueryBuilder.distance(100, DistanceUnit.KILOMETERS);
        boolQueryBuilder.must(geoDistanceQueryBuilder);
        boolQueryBuilder.must(QueryBuilders.termQuery("authority", 0));
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        // 设置排序
        GeoDistanceSortBuilder geoDistanceSortBuilder = new GeoDistanceSortBuilder("geoPoint", pageParam.getLatitude(), pageParam.getLongitude());
        geoDistanceSortBuilder.unit(DistanceUnit.KILOMETERS);
        geoDistanceSortBuilder.order(SortOrder.ASC);
        nativeSearchQueryBuilder.withSorts(geoDistanceSortBuilder);
        List<NotesVO> notesList = getNotesList(nativeSearchQueryBuilder);
        NotesPageVO notesPageVO = new NotesPageVO();
        notesPageVO.setList(notesList);
        notesPageVO.setPage(pageParam.getPage());
        notesPageVO.setPageSize(pageParam.getPageSize());
        long count = elasticsearchRestTemplate.count(nativeSearchQueryBuilder.build(), NotesEsDO.class);
        notesPageVO.setTotal((int) count);
        return ResultUtil.successGet(notesPageVO);
    }

    /**
     * 更新笔记
     *
     * @param notesDO 笔记
     */
    @Override
    public void updateNotes(NotesDO notesDO) {
        NotesEsDO notesEsDO = new NotesEsDO();
        BeanUtils.copyProperties(notesDO, notesEsDO);
        String s = HtmlParseUtils.htmlToText(notesEsDO.getContent());
        notesEsDO.setTextContent(s);
        Date updateTime = notesDO.getUpdateTime();
        notesEsDO.setUpdateTime(updateTime.getTime());
        GeoPoint geoPoint = new GeoPoint(notesDO.getLatitude(), notesDO.getLongitude());
        notesEsDO.setGeoPoint(geoPoint);
        // 先删除再添加
        Query query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.termQuery("id", notesDO.getId())).build();
        elasticsearchRestTemplate.delete(query, NotesEsDO.class);
        elasticsearchRestTemplate.save(notesEsDO);
    }

    /**
     * 删除笔记
     *
     * @param notesId 笔记id
     */
    @Override
    public void deleteNotes(Long notesId) {
        Query query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.termQuery("id", notesId)).build();
        elasticsearchRestTemplate.delete(query, NotesEsDO.class);
    }

    /**
     * 根据关键字搜索笔记
     *
     * @param keyword  关键字
     * @param page     页码
     * @param pageSize 每页数量
     * @param noteType 笔记类型
     * @param hot      热度
     * @return 笔记列表
     */
    @Override
    public Result<NotesPageVO> getNotesByKeyword(String keyword, Integer page, Integer pageSize, Integer noteType, Integer hot) {
        // 判断keyword是否为空，并去除所有空格
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        } else {
            keyword = keyword.replaceAll(" ", "");
        }
        if (keyword.length() > 20) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        // 设置分页
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize);
        nativeSearchQueryBuilder.withPageable(pageRequest);
        // 设置查询条件
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.termQuery("authority", 0));
        // 0：图片笔记，1：视频笔记，2：全部
        if (noteType == 0) {
            boolQueryBuilder.must(QueryBuilders.termQuery("notesType", 0));
        } else if (noteType == 1) {
            boolQueryBuilder.must(QueryBuilders.termQuery("notesType", 1));
        }
        // 0：最新，1：最热，2：全部
        if (hot == 0) {
            FieldSortBuilder sortBuilder = SortBuilders.fieldSort("createTime").order(SortOrder.DESC);
            nativeSearchQueryBuilder.withSorts(sortBuilder);
        } else if (hot == 1) {
            FieldSortBuilder sortBuilder = SortBuilders.fieldSort("notesLikeNum").order(SortOrder.DESC);
            nativeSearchQueryBuilder.withSorts(sortBuilder);
        }
        // 设置查询条件，标题和内容中包含keyword，且匹配度大于70%，标题权重为10，内容权重为5
        boolQueryBuilder.must(QueryBuilders.multiMatchQuery(keyword, "title", "textContent")
                .minimumShouldMatch("70%").field("title", 10)
                .field("textContent", 5));
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        List<NotesVO> notesList = getNotesList(nativeSearchQueryBuilder);
        NotesPageVO notesPageVO = new NotesPageVO();
        notesPageVO.setList(notesList);
        notesPageVO.setPage(page);
        notesPageVO.setPageSize(pageSize);
        long count = elasticsearchRestTemplate.count(nativeSearchQueryBuilder.build(), NotesEsDO.class);
        notesPageVO.setTotal((int) count);
        return ResultUtil.successGet(notesPageVO);
    }

    /**
     * 获取笔记列表
     *
     * @param nativeSearchQueryBuilder 查询条件
     * @return 笔记列表
     */
    private List<NotesVO> getNotesList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        SearchHits<NotesEsDO> searchHits = elasticsearchRestTemplate.search(nativeSearchQueryBuilder.build(), NotesEsDO.class);
        return searchHits.get().map(hit -> {
            NotesEsDO content = hit.getContent();
            NotesVO notesVO = new NotesVO();
            BeanUtils.copyProperties(content, notesVO);
            Result<?> result = userFeign.getUserInfo(content.getBelongUserId());
            if (result.getCode() == 20010) {
                Map<String, Object> userInfo = (Map<String, Object>) result.getData();
                notesVO.setNickname((String) userInfo.get("nickname"));
                notesVO.setAvatarUrl((String) userInfo.get("avatarUrl"));
            }
            Object notesLikeNum = redisCache.hget(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, content.getId().toString()), "notesLikeNum");
            if (Objects.isNull(notesLikeNum)) {
                notesVO.setNotesLikeNum(content.getNotesLikeNum());
                redisCache.hset(RedisKey.build(RedisConstant.REDIS_KEY_NOTES_COUNT, content.getId().toString()), "notesLikeNum", content.getNotesLikeNum());
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
                    Boolean isLike = Objects.nonNull(redisCache.zSetScore(key, content.getId()));
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
    }

    /**
     * 更新点赞数或收藏数
     *
     * @param map 参数
     */
    @Override
    public void updateCount(Map<String, String> map) {
        String type = map.get("type");
        long notesId = Long.parseLong(map.get("notesId"));
        Query query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.termQuery("id", notesId)).build();
        if ("like".equals(type)) {
            // 更新es中的点赞数
            NotesEsDO content = Objects.requireNonNull(elasticsearchRestTemplate.searchOne(query, NotesEsDO.class)).getContent();
            content.setNotesLikeNum(Integer.parseInt(map.get("notesLikeNum")));
            elasticsearchRestTemplate.save(content);
        } else if ("collection".equals(type)) {
            // 更新es中的收藏数
            NotesEsDO content = Objects.requireNonNull(elasticsearchRestTemplate.searchOne(query, NotesEsDO.class)).getContent();
            content.setNotesCollectionNum(Integer.parseInt(map.get("notesCollectionNum")));
            elasticsearchRestTemplate.save(content);
        }
    }
}
