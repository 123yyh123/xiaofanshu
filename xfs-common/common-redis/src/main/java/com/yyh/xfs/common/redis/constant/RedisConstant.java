package com.yyh.xfs.common.redis.constant;

/**
 * @author yyh
 * @date 2023-12-11
 * redis常量
 */
public class RedisConstant {
    /**
     * redis key 用户登录过期前缀
     */
    public static final String REDIS_KEY_USER_LOGIN_EXPIRE = "user:login:expire:";
    /**
     * redis key 用户登录信息前缀
     */
    public static final String REDIS_KEY_USER_LOGIN_INFO= "user:login:info:";
    /**
     * redis key 在线用户set集合key
     */
    public static final String REDIS_KEY_USER_ONLINE = "user:online";
    /**
     * redis key 需要更新用户信息的集合key
     */
    public static final String REDIS_KEY_USER_INFO_UPDATE_LIST = "user:info:update:list";
    /**
     * redis key 用户离线消息前缀
     */
    public static final String REDIS_KEY_USER_OFFLINE_MESSAGE = "user:offline:message:";
    /**
     * redis key 注册短信验证码前缀
     */
    public static final String REDIS_KEY_SMS_REGISTER_PHONE_CODE = "sms:register:phone:code:";
    /**
     * redis key 绑定短信验证码前缀
     */
    public static final String REDIS_KEY_SMS_BIND_PHONE_CODE = "sms:bind:phone:code:";
    /**
     * redis key 登录短信验证码前缀
     */
    public static final String REDIS_KEY_SMS_LOGIN_PHONE_CODE = "sms:login:phone:code:";
    /**
     * redis key 重置密码短信验证码前缀
     */
    public static final String REDIS_KEY_SMS_RESET_PASSWORD_PHONE_CODE = "sms:reset:password:phone:code:";

    /**
     * redis key 用户关系是否允许发送消息前缀
     */
    public static final String REDIS_KEY_USER_RELATION_ALLOW_SEND_MESSAGE = "user:relation:allow:send:message:";

}
