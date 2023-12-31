package com.yyh.xfs.common.myEnum;

import com.yyh.xfs.common.constant.StatusCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public enum ExceptionMsgEnum {
    /**
     * 业务异常
     */
    NOT_LOGIN(StatusCode.NOT_LOGIN,"未登录"),
    TOKEN_EXPIRED(StatusCode.TOKEN_EXPIRED,"token过期"),
    TOKEN_INVALID(StatusCode.TOKEN_INVALID,"token不合法"),
    PASSWORD_ERROR(StatusCode.PASSWORD_ERROR,"密码错误"),
    SMS_CODE_ERROR(StatusCode.SMS_CODE_ERROR,"验证码错误"),
    PHONE_NUMBER_EXIST(StatusCode.PHONE_NUMBER_EXIST,"手机号已绑定"),
    PHONE_NUMBER_NOT_REGISTER(StatusCode.PHONE_NUMBER_NOT_REGISTER,"手机号未注册"),
    LOGIN_TYPE_ERROR(StatusCode.LOGIN_TYPE_ERROR,"登录类型错误"),
    OPEN_ID_NULL(StatusCode.OPEN_ID_NULL,"账号openId为空"),
    ACCOUNT_EXCEPTION(StatusCode.ACCOUNT_EXCEPTION,"账号异常"),
    PARAMETER_ERROR(StatusCode.PARAMETER_ERROR,"参数错误"),
    FILE_SIZE_TOO_LARGE(StatusCode.FILE_SIZE_TOO_LARGE,"文件过大"),
    SMS_CODE_SEND_FREQUENTLY(StatusCode.SMS_CODE_SEND_FREQUENTLY,"短信发送频繁,请稍后再试"),
    /**
     * 系统异常
     */
    ALIYUN_SMS_SEND_ERROR(StatusCode.ALIYUN_SMS_SEND_ERROR,"短信发送失败"),
    ALIYUN_SMS_INIT_ERROR(StatusCode.ALIYUN_SMS_INIT_ERROR,"短信服务初始化异常"),
    ALIYUN_OSS_INIT_ERROR(StatusCode.ALIYUN_OSS_INIT_ERROR,"OSS服务初始化异常"),
    REDIS_ERROR(StatusCode.REDIS_ERROR,"redis操作异常"),
    DB_ERROR(StatusCode.DB_ERROR,"数据库操作异常");
    private Integer code;
    private String msg;

    ExceptionMsgEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
