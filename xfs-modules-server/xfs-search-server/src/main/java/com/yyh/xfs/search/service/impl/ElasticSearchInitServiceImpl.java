package com.yyh.xfs.search.service.impl;

import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.web.exception.SystemException;
import com.yyh.xfs.search.domain.NotesEsDO;
import com.yyh.xfs.search.service.ElasticSearchInitService;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

/**
 * @author yyh
 * @date 2024-01-24
 */
@Service
public class ElasticSearchInitServiceImpl implements ElasticSearchInitService {
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    public ElasticSearchInitServiceImpl(ElasticsearchRestTemplate elasticsearchRestTemplate) {
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
    }

    @Override
    public void initElasticSearch() {
        // 判断索引是否存在
        try {
            if (!elasticsearchRestTemplate.indexOps(NotesEsDO.class).exists()) {
                // 创建索引
                elasticsearchRestTemplate.indexOps(NotesEsDO.class).create();
                // 创建映射
                elasticsearchRestTemplate.indexOps(NotesEsDO.class).putMapping();
            }
        } catch (Exception e) {
            throw new SystemException(ExceptionMsgEnum.ELASTICSEARCH_INIT_ERROR, e);
        }
    }
}
