package com.yyh.xfs.search.controller;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.redis.constant.RedisConstant;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.common.redis.utils.RedisKey;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.common.web.exception.BusinessException;
import com.yyh.xfs.search.service.ElasticSearchInitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyh
 * @date 2024-01-24
 */
@RestController
@RequestMapping("/search/init")
public class ElasticSearchInitController {

    @Value("${spring.elasticsearch.init.key}")
    private String key;

    private final ElasticSearchInitService elasticSearchInitService;
    private final RedisCache redisCache;

    public ElasticSearchInitController(RedisCache redisCache, ElasticSearchInitService elasticSearchInitService) {
        this.redisCache = redisCache;
        this.elasticSearchInitService = elasticSearchInitService;
    }
    @GetMapping("/{key}/{type}")
    public Result<?> initElasticSearch(@PathVariable String key, @PathVariable String type) {
        if (!StringUtils.hasText(key) || !this.key.equals(key) || !StringUtils.hasText(type)) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        Boolean setnx = redisCache.setnx(RedisKey.build(RedisConstant.REDIS_KEY_ELASTICSEARCH_INIT, key + ":" + type));
        if (!setnx) {
            throw new BusinessException(ExceptionMsgEnum.ELASTICSEARCH_INIT_ALREADY);
        }
        elasticSearchInitService.initElasticSearch();
        return ResultUtil.successGet(null);
    }

}
