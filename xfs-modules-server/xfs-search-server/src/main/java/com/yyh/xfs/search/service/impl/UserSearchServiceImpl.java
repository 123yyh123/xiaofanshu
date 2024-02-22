package com.yyh.xfs.search.service.impl;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.common.web.exception.BusinessException;
import com.yyh.xfs.search.domain.NotesEsDO;
import com.yyh.xfs.search.domain.UserEsDO;
import com.yyh.xfs.search.service.UserSearchService;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author yyh
 * @date 2024-02-21
 */
@Service
public class UserSearchServiceImpl implements UserSearchService {
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    public UserSearchServiceImpl(ElasticsearchRestTemplate elasticsearchRestTemplate) {
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
    }

    @Override
    public Result<List<UserEsDO>> getUser(String keyword, Integer page, Integer pageSize) {
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
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 设置查询条件
        boolQueryBuilder.must(QueryBuilders.multiMatchQuery(keyword, "nickname", "uid").minimumShouldMatch("70%"));
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        SearchHits<UserEsDO> searchHits = elasticsearchRestTemplate.search(nativeSearchQueryBuilder.build(), UserEsDO.class);
        List<UserEsDO> userEsDOList = searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
        return ResultUtil.successGet(userEsDOList);
    }

    @Override
    public void addUser(UserEsDO userEsDO) {
        elasticsearchRestTemplate.save(userEsDO);
    }

    @Override
    public void updateUser(UserEsDO userEsDO) {
        UpdateQuery updateQuery = UpdateQuery.builder(userEsDO.getId().toString())
                .withDocument(Document.create())
                .build();
        if (StringUtils.hasText(userEsDO.getNickname())) {
            Objects.requireNonNull(updateQuery.getDocument()).append("nickname", userEsDO.getNickname());
        }
        if (StringUtils.hasText(userEsDO.getAvatarUrl())) {
            Objects.requireNonNull(updateQuery.getDocument()).append("avatarUrl", userEsDO.getAvatarUrl());
        }
        if (!Objects.requireNonNull(updateQuery.getDocument()).isEmpty()) {
            elasticsearchRestTemplate.update(updateQuery, IndexCoordinates.of("user"));
        }
    }
}
