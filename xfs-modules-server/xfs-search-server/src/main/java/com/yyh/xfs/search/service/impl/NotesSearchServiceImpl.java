package com.yyh.xfs.search.service.impl;

import com.yyh.xfs.common.domain.PageParam;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.redis.constant.RedisConstant;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.common.redis.utils.RedisKey;
import com.yyh.xfs.common.utils.ResultUtil;
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
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.servlet.http.HttpServletRequest;
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

    @Override
    public void addNotes(NotesDO notesDO) {
        NotesEsDO notesEsDO = new NotesEsDO();
        BeanUtils.copyProperties(notesDO, notesEsDO);
        // 重新设置时间，因为es中的时间会自动减去8小时，时区问题
        Date createTime = notesDO.getCreateTime();
        createTime.setTime(createTime.getTime() + 8 * 60 * 60 * 1000);
        notesEsDO.setCreateTime(createTime);
        Date updateTime = notesDO.getUpdateTime();
        updateTime.setTime(updateTime.getTime() + 8 * 60 * 60 * 1000);
        notesEsDO.setUpdateTime(updateTime);
        GeoPoint geoPoint = new GeoPoint(notesDO.getLatitude(), notesDO.getLongitude());
        notesEsDO.setLocation(geoPoint);
        elasticsearchRestTemplate.save(notesEsDO);
    }

    @Override
    public Result<NotesPageVO> getNotesNearBy(PageParam pageParam) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        // 设置分页
        PageRequest pageRequest = PageRequest.of(pageParam.getPage() - 1, pageParam.getPageSize());
        nativeSearchQueryBuilder.withPageable(pageRequest);
        // 设置查询条件，默认距离100km的笔记，authority为0,表示公开
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        GeoDistanceQueryBuilder geoDistanceQueryBuilder = new GeoDistanceQueryBuilder("location");
        geoDistanceQueryBuilder.point(pageParam.getLatitude(), pageParam.getLongitude());
        geoDistanceQueryBuilder.distance(100, DistanceUnit.KILOMETERS);
        boolQueryBuilder.must(geoDistanceQueryBuilder);
        boolQueryBuilder.must(QueryBuilders.termQuery("authority", 0));
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        // 设置排序
        GeoDistanceSortBuilder geoDistanceSortBuilder = new GeoDistanceSortBuilder("location", pageParam.getLatitude(), pageParam.getLongitude());
        geoDistanceSortBuilder.unit(DistanceUnit.KILOMETERS);
        geoDistanceSortBuilder.order(SortOrder.ASC);
        nativeSearchQueryBuilder.withSorts(geoDistanceSortBuilder);
        SearchHits<NotesEsDO> searchHits = elasticsearchRestTemplate.search(nativeSearchQueryBuilder.build(), NotesEsDO.class);
        List<NotesVO> list = searchHits.get().map(hit -> {
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
        NotesPageVO notesPageVO = new NotesPageVO();
        notesPageVO.setList(list);
        notesPageVO.setPage(pageParam.getPage());
        notesPageVO.setPageSize(pageParam.getPageSize());
        long count = elasticsearchRestTemplate.count(nativeSearchQueryBuilder.build(), NotesEsDO.class);
        notesPageVO.setTotal((int) count);
        return ResultUtil.successGet(notesPageVO);
    }

    @Override
    public void updateNotes(NotesDO notesDO) {
        NotesEsDO notesEsDO = new NotesEsDO();
        BeanUtils.copyProperties(notesDO, notesEsDO);
        // 重新设置时间，因为es中的时间会自动减去8小时，时区问题
        Date updateTime = notesDO.getUpdateTime();
        updateTime.setTime(updateTime.getTime() + 8 * 60 * 60 * 1000);
        notesEsDO.setUpdateTime(updateTime);
        GeoPoint geoPoint = new GeoPoint(notesDO.getLatitude(), notesDO.getLongitude());
        notesEsDO.setLocation(geoPoint);
        // 先删除再添加
        Query query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.termQuery("id", notesDO.getId())).build();
        elasticsearchRestTemplate.delete(query, NotesEsDO.class);
        elasticsearchRestTemplate.save(notesEsDO);
    }

    @Override
    public void deleteNotes(Long notesId) {
        Query query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.termQuery("id", notesId)).build();
        elasticsearchRestTemplate.delete(query, NotesEsDO.class);
    }
}
