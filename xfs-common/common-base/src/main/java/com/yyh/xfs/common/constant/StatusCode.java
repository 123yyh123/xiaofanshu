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
    public static final String NOT_LOGIN_MSG = "未登录";
    //token过期
    public static final Integer TOKEN_EXPIRED = 40320;
    public static final String TOKEN_EXPIRED_MSG = "token过期";
    //token无效
    public static final Integer TOKEN_INVALID = 40330;
    public static final String TOKEN_INVALID_MSG = "token不合法";
}
