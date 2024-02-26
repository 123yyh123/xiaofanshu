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
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author yyh
 * @date 2024-02-24
 */
@Aspect
@Slf4j
@Component
public class BloomFilterAspect {

    private final BloomFilterUtils bloomFilterUtils;

    public BloomFilterAspect(BloomFilterUtils bloomFilterUtils) {
        this.bloomFilterUtils = bloomFilterUtils;
    }

    /**
     * 切点
     */
    @Pointcut("@annotation(bloomFilterProcessing)")
    public void bloomFilterPointcut(BloomFilterProcessing bloomFilterProcessing) {
    }

    /**
     * 环绕通知
     *
     * @param point 切点
     * @return Object
     * @throws Throwable 异常
     */
    @Around(value = "bloomFilterPointcut(bloomFilterProcessing)", argNames = "point,bloomFilterProcessing")
    public Object around(ProceedingJoinPoint point, BloomFilterProcessing bloomFilterProcessing) throws Throwable {
        // 获取注解中的布隆过滤器名称
        String map = bloomFilterProcessing.map();
        // 获取注解中的key
        Object[] args = point.getArgs();
        MethodSignature signature = (MethodSignature) point.getSignature();
        Class<?> targetClass = point.getTarget().getClass();
        Method method = targetClass.getDeclaredMethod(signature.getName(), signature.getMethod().getParameterTypes());
        ExpressionParser parser = new SpelExpressionParser();
        ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
        Arrays.stream(bloomFilterProcessing.keys()).map(parser::parseExpression).forEach(expression -> {
            EvaluationContext context = new MethodBasedEvaluationContext(TypedValue.NULL, method, args, parameterNameDiscoverer);
            Object value = expression.getValue(context);
            if (value == null) {
                throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
            }
            Long id = Long.valueOf(value.toString());
            log.info("map:{},id:{}", map, id);
            if (!bloomFilterUtils.mightContainBloomFilter(map, String.valueOf(id))) {
                throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
            }
        });
        return point.proceed();
    }

}
