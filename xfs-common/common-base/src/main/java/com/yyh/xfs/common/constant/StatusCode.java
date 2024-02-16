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
    public static final Integer PASSWORD_ERROR = 40340;
    //手机号已存在
    public static final Integer PHONE_NUMBER_EXIST = 10010;
    //手机号未注册
    public static final Integer PHONE_NUMBER_NOT_REGISTER = 10011;
    //短信验证码错误
    public static final Integer SMS_CODE_ERROR = 10020;
    //登录类型错误
    public static final Integer LOGIN_TYPE_ERROR = 10030;
    //第三方账号openId为空
    public static final Integer OPEN_ID_NULL = 10040;
    //短信发送频繁
    public static final Integer SMS_CODE_SEND_FREQUENTLY = 10050;
    //账号异常
    public static final Integer ACCOUNT_EXCEPTION = 10060;
    //账号在其他设备登录
    public static final Integer ACCOUNT_OTHER_LOGIN = 10061;
    //账号操作异常
    public static final Integer ACCOUNT_OPERATION_ERROR = 10062;
    //文件过大
    public static final Integer FILE_SIZE_TOO_LARGE = 10070;
    //ElasticSearch已初始化
    public static final Integer ELASTICSEARCH_INIT_ALREADY = 10080;
    // ElasticSearch初始化异常
    public static final Integer ELASTICSEARCH_INIT_ERROR = 10081;
    //获取地理信息失败
    public static final Integer GET_GEOGRAPHIC_INFORMATION_ERROR = 10090;
    //无权限
    public static final Integer NO_PERMISSION = 10100;
    //参数错误
    public static final Integer PARAMETER_ERROR = 40010;
    //服务器异常
    public static final Integer SERVER_ERROR = 50000;
    //数据库操作异常
    public static final Integer DB_ERROR = 50010;
    //aliyun短信服务初始化异常
    public static final Integer ALIYUN_SMS_INIT_ERROR = 50020;
    //aliyun短信服务异常
    public static final Integer ALIYUN_SMS_SEND_ERROR = 50021;
    //aliyun oss服务异常
    public static final Integer ALIYUN_OSS_INIT_ERROR = 50030;
    public static final Integer REDIS_ERROR = 50040;
    //文件不能为空
    public static final Integer FILE_NOT_NULL = 50050;
}
