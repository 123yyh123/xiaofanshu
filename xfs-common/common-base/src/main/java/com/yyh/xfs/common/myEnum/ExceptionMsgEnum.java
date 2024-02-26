package com.yyh.xfs.common.myEnum;

import com.yyh.xfs.common.constant.StatusCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author yyh
 * @date 2023-12-11
 */

@Getter
@ToString
public enum ExceptionMsgEnum {
    /**
     * 业务异常
     */
    NOT_LOGIN(StatusCode.NOT_LOGIN,"未登录"),
    TOKEN_EXPIRED(StatusCode.TOKEN_EXPIRED,"登录状态过期"),
    TOKEN_INVALID(StatusCode.TOKEN_INVALID,"登录状态无效"),
    ACCOUNT_OPERATION_ERROR(StatusCode.ACCOUNT_OPERATION_ERROR,"账号操作异常"),
    ACCOUNT_OTHER_LOGIN(StatusCode.ACCOUNT_OTHER_LOGIN,"账号在其他设备登录"),
    PASSWORD_ERROR(StatusCode.PASSWORD_ERROR,"密码错误"),
    SMS_CODE_ERROR(StatusCode.SMS_CODE_ERROR,"验证码错误"),
    PHONE_NUMBER_EXIST(StatusCode.PHONE_NUMBER_EXIST,"手机号已绑定"),
    PHONE_NUMBER_NOT_REGISTER(StatusCode.PHONE_NUMBER_NOT_REGISTER,"手机号未注册"),
    GET_GEOGRAPHIC_INFORMATION_ERROR(StatusCode.GET_GEOGRAPHIC_INFORMATION_ERROR,"获取地理信息失败"),
    LOGIN_TYPE_ERROR(StatusCode.LOGIN_TYPE_ERROR,"登录类型错误"),
    OPEN_ID_NULL(StatusCode.OPEN_ID_NULL,"账号openId为空"),
    ACCOUNT_EXCEPTION(StatusCode.ACCOUNT_EXCEPTION,"账号异常"),
    PARAMETER_ERROR(StatusCode.PARAMETER_ERROR,"参数错误"),
    FILE_SIZE_TOO_LARGE(StatusCode.FILE_SIZE_TOO_LARGE,"文件过大"),
    FILE_NOT_NULL(StatusCode.FILE_NOT_NULL,"文件不能为空"),
    ELASTICSEARCH_INIT_ALREADY(StatusCode.ELASTICSEARCH_INIT_ALREADY,"ElasticSearch已初始化"),
    SMS_CODE_SEND_FREQUENTLY(StatusCode.SMS_CODE_SEND_FREQUENTLY,"短信发送频繁,请稍后再试"),
    NO_PERMISSION(StatusCode.NO_PERMISSION,"无权限"),
    REPEAT_OPERATION(StatusCode.REPEAT_OPERATION,"操作太过频繁"),
    /**
     * 系统异常
     */
    ALIYUN_SMS_SEND_ERROR(StatusCode.ALIYUN_SMS_SEND_ERROR,"短信发送失败"),
    ALIYUN_SMS_INIT_ERROR(StatusCode.ALIYUN_SMS_INIT_ERROR,"短信服务初始化异常"),
    ALIYUN_OSS_INIT_ERROR(StatusCode.ALIYUN_OSS_INIT_ERROR,"OSS服务初始化异常"),
    ELASTICSEARCH_INIT_ERROR(StatusCode.ELASTICSEARCH_INIT_ERROR,"ElasticSearch初始化异常"),
    REDIS_ERROR(StatusCode.REDIS_ERROR,"redis操作异常"),
    SERVER_ERROR(StatusCode.SERVER_ERROR,"服务器异常"),
    DB_ERROR(StatusCode.DB_ERROR,"数据库操作异常");
    private Integer code;
    private String msg;

    ExceptionMsgEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
