package com.yyh.xfs.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyh.xfs.common.constant.ReleasePath;
import com.yyh.xfs.common.constant.StatusCode;
import com.yyh.xfs.common.redis.constant.RedisConstant;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.gateway.properties.JwtProperties;
import com.yyh.xfs.gateway.utils.JWTUtil;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yyh
 * @date 2023-11-30
 */
@Component
@Slf4j
public class JWTFilter implements GlobalFilter {

    private final JwtProperties jwtProperties;
    private final RedisCache redisCache;

    public JWTFilter(RedisCache redisCache, JwtProperties jwtProperties) {
        this.redisCache = redisCache;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //放行登录请求，退出登录请求
        String path = exchange.getRequest().getURI().getPath();
        if (path.contains(ReleasePath.LOGIN)
                || path.contains(ReleasePath.LOGOUT)
                || path.contains(ReleasePath.REGISTER)
                || path.contains(ReleasePath.SEND_BIND_PHONE_CODE)
                || path.contains(ReleasePath.OTHER_LOGIN)
                || path.contains(ReleasePath.BIND_PHONE)
                || path.contains(ReleasePath.SEND_RESET_PASSWORD_PHONE_CODE)
                || path.contains(ReleasePath.RESET_PASSWORD)) {
            return chain.filter(exchange);
        }
        String token = exchange.getRequest().getHeaders().getFirst("token");
        ServerHttpResponse response = exchange.getResponse();
        if (!StringUtils.hasText(token)) {
            // token为空
            return tokenFailure(response,StatusCode.NOT_LOGIN,StatusCode.NOT_LOGIN_MSG);
        }
        try {
            Map<String, Object> map = JWTUtil.parseToken(token);
            if (map == null || !StringUtils.hasText((String) map.get("userId"))) {
                // token不合法
                return tokenFailure(response,StatusCode.TOKEN_INVALID,StatusCode.TOKEN_INVALID_MSG);
            }
            // 判断token是否过期
            Long expire = (Long) redisCache.get(RedisConstant.REDIS_KEY_USER_LOGIN_EXPIRE + map.get("userId"));
            if (expire == null || expire < System.currentTimeMillis()) {
                // token过期
                return tokenFailure(response,StatusCode.TOKEN_EXPIRED,StatusCode.TOKEN_EXPIRED_MSG);
            }
            // 刷新token的过期时间
            if(expire - System.currentTimeMillis() < jwtProperties.getRefreshTime()){
                redisCache.set(RedisConstant.REDIS_KEY_USER_LOGIN_EXPIRE + map.get("userId"),System.currentTimeMillis() + jwtProperties.getExpireTime());
            }
        } catch (Exception e) {
            // token不合法
            return tokenFailure(response,StatusCode.TOKEN_INVALID,StatusCode.TOKEN_INVALID_MSG);
        }
        //TODO:这里可以做权限校验
        return chain.filter(exchange);
    }

    private Mono<Void> tokenFailure(ServerHttpResponse response,Integer code,String msg) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("msg", msg);
        try {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(objectMapper.writeValueAsBytes(map))));
        } catch (JsonProcessingException e) {
            log.error("写入响应失败", e);
            return response.setComplete();
        }
    }
}
