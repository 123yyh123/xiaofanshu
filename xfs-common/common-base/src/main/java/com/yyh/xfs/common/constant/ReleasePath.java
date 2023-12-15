package com.yyh.xfs.common.constant;

/**
 * @author yyh
 * @date 2023-12-11
 */
public class ReleasePath {
    /**
     * 登录
     */
    public static final String LOGIN = "/auth/login";
    /**
     * 注册
     */
    public static final String REGISTER = "/auth/register";
    /**
     * 发送绑定手机号验证码
     */
    public static final String SEND_BIND_PHONE_CODE = "/third/sendBindPhoneSms";
    /**
     * 发送重置密码手机号验证码
     */
    public static final String SEND_RESET_PASSWORD_PHONE_CODE = "/third/sendResetPhoneSms";
    /**
     * 重置密码
     */
    public static final String RESET_PASSWORD = "/user/resetPassword";
    /**
     * 登出
     */
    public static final String LOGOUT = "/auth/logout";
    /**
     * 其他登录
     */
    public static final String OTHER_LOGIN = "/auth/otherLogin";
    /**
     * 绑定手机号
     */
    public static final String BIND_PHONE = "/auth/bindPhone";
}
