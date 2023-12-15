package com.yyh.xfs.common.myEnum;

import com.yyh.xfs.common.constant.StatusCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author yyh
 * @date 2023-12-15
 * @desc 业务异常枚举
 */
@Getter
@ToString
public enum ExceptionMsgEnum {
    NOT_LOGIN(StatusCode.NOT_LOGIN,"未登录"),
    TOKEN_EXPIRED(StatusCode.TOKEN_EXPIRED,"token过期"),
    TOKEN_INVALID(StatusCode.TOKEN_INVALID,"token不合法"),
    PASSWORD_ERROR(StatusCode.PASSWORD_ERROR,"密码错误"),
    SMS_CODE_ERROR(StatusCode.SMS_CODE_ERROR,"验证码错误"),
    PHONE_NUMBER_EXIST(StatusCode.PHONE_NUMBER_EXIST,"手机号已存在"),
    LOGIN_TYPE_ERROR(StatusCode.LOGIN_TYPE_ERROR,"登录类型错误"),
    OPEN_ID_NULL(StatusCode.OPEN_ID_NULL,"账号openId为空"),
    SMS_CODE_SEND_FREQUENTLY(StatusCode.SMS_CODE_SEND_FREQUENTLY,"短信发送频繁,请稍后再试"),


    ALIYUN_SMS_SEND_ERROR(StatusCode.ALIYUN_SMS_SEND_ERROR,"短信服务异常"),
    DB_ERROR(StatusCode.DB_ERROR,"数据库操作异常");
    private Integer code;
    private String msg;

    ExceptionMsgEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
