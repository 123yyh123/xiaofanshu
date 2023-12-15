package com.yyh.xfs.common.constant;

/**
 * @author yyh
 * @date 2023-12-09
 * @desc 状态码
 */
public class StatusCode {
    public static final Integer GET_SUCCESS = 20010;
    public static final Integer GET_ERROR = 20011;

    public static final Integer POST_SUCCESS = 20020;
    public static final Integer POST_ERROR = 20021;

    public static final Integer PUT_SUCCESS = 20030;
    public static final Integer PUT_ERROR = 20031;

    public static final Integer DELETE_SUCCESS = 20040;
    public static final Integer DELETE_ERROR = 20041;

    //未登录
    public static final Integer NOT_LOGIN = 40310;
    //token过期
    public static final Integer TOKEN_EXPIRED = 40320;
    //token无效
    public static final Integer TOKEN_INVALID = 40330;
    //密码错误
    public static final Integer PASSWORD_ERROR=40340;
    //手机号已存在
    public static final Integer PHONE_NUMBER_EXIST=10010;
    //短信验证码错误
    public static final Integer SMS_CODE_ERROR=10020;
    //登录类型错误
    public static final Integer LOGIN_TYPE_ERROR = 10030;
    //第三方账号openId为空
    public static final Integer OPEN_ID_NULL = 10040;
    //短信发送频繁
    public static final Integer SMS_CODE_SEND_FREQUENTLY = 10050;
    //数据库操作异常
    public static final Integer DB_ERROR=50010;
    //aliyun短信服务初始化异常
    public static final Integer ALIYUN_SMS_INIT_ERROR=50020;
    //aliyun短信服务异常
    public static final Integer ALIYUN_SMS_SEND_ERROR=50021;
}
