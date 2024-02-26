package com.yyh.xfs.common.web.aop.idempotent;

import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.redis.constant.RedisConstant;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.common.redis.utils.RedisKey;
import com.yyh.xfs.common.web.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author yyh
 * @date 2024-02-26
 */
@Aspect
@Slf4j
@Component
public class IdempotentAspect {
    private final HttpServletRequest request;
    private final RedisCache redisCache;

    public IdempotentAspect(HttpServletRequest request, RedisCache redisCache) {
        this.request = request;
        this.redisCache = redisCache;
    }

    /**
     * 切点
     */
    @Pointcut("@annotation(idempotent)")
    public void idempotentPointcut(Idempotent idempotent) {
    }

    /**
     * 环绕通知
     *
     * @param point      切点
     * @param idempotent 幂等注解
     * @return Object
     */
    @Around(value = "idempotentPointcut(idempotent)", argNames = "point,idempotent")
    public Object around(ProceedingJoinPoint point, Idempotent idempotent) throws Throwable {
        // 获取token
        String token = request.getHeader("token");
        String redisKey = getRedisKey(point, idempotent, token);
        // 获取过期时间
        long expire = idempotent.expireTime();
        // 利用redis的setnx实现幂等
        boolean result = redisCache.setnxAndExpire(redisKey,expire, TimeUnit.MILLISECONDS);
        if (!result) {
            log.error("重复操作");
            throw new BusinessException(ExceptionMsgEnum.REPEAT_OPERATION);
        }else {
            return point.proceed();
        }
    }

    private static String getRedisKey(ProceedingJoinPoint point, Idempotent idempotent, String token) {
        if (token == null) {
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        // 获取注解中的唯一标识
        String value = idempotent.value();
        // 获取方法名
        MethodSignature signature = (MethodSignature) point.getSignature();
        String methodName = signature.getMethod().getName();
        // 拼接唯一后缀标识，token + 路径 + 方法名 + 参数
        String suffix = token +"_"+ value+"_"+ methodName+ "_" + Arrays.toString(point.getArgs());
        return RedisKey.build(RedisConstant.REDIS_KEY_IDEMPOTENT, suffix);
    }
}
