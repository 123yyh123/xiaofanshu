package com.yyh.xfs.common.web.aop.bloomFilter;

import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.redis.utils.BloomFilterUtils;
import com.yyh.xfs.common.web.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author yyh
 * @date 2024-02-24
 */
@Aspect
@Slf4j
@Component
public class BloomFilterAspect {

    @Autowired
    private BloomFilterUtils bloomFilterUtils;

    /**
     * 切点
     */
    @Pointcut("@annotation(bloomFilterProcessing)")
    public void bloomFilterPointcut(BloomFilterProcessing bloomFilterProcessing) {}

    /**
     * 环绕通知
     * @param point 切点
     * @return Object
     * @throws Throwable 异常
     */
    @Around("bloomFilterPointcut(bloomFilterProcessing)")
    public Object around(ProceedingJoinPoint point, BloomFilterProcessing bloomFilterProcessing) throws Throwable {
        // 获取注解中的布隆过滤器名称
        String map = bloomFilterProcessing.map();
        // 获取注解中的key
        Object[] args = point.getArgs();
        // 检查参数是否存在且非空
        if (args == null || args.length == 0) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        Arrays.stream(args).forEach(arg -> {
            Long id =(Long) arg;
            log.info("map:{},id:{}",map,id);
            if (!bloomFilterUtils.mightContainBloomFilter(map, String.valueOf(id))) {
                throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
            }
        });
        return point.proceed();
    }

}
