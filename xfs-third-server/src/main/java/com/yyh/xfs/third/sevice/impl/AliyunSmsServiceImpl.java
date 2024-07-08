package com.yyh.xfs.third.sevice.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.redis.constant.RedisConstant;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.common.redis.utils.RedisKey;
import com.yyh.xfs.common.utils.CodeUtil;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.common.web.exception.BusinessException;
import com.yyh.xfs.common.web.exception.SystemException;
import com.yyh.xfs.third.sevice.AliyunSmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author yyh
 * @date 2023-12-15
 */
@Service
@Slf4j
public class AliyunSmsServiceImpl implements AliyunSmsService {
    private final RedisCache redisCache;
    private final Client smsClient;

    public AliyunSmsServiceImpl(RedisCache redisCache, Client smsClient) {
        this.redisCache = redisCache;
        this.smsClient = smsClient;
    }

    @Override
    public Result<?> sendBindPhoneSms(String phoneNumber) {
        String smsCode = CodeUtil.createSmsCode();
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setSignName("阿里云短信测试")
                .setTemplateCode("SMS_154950909")
                .setPhoneNumbers(phoneNumber)
                .setTemplateParam("{\"code\":\"" + smsCode + "\"}");
        return sendSms(sendSmsRequest, phoneNumber, smsCode, RedisConstant.REDIS_KEY_SMS_BIND_PHONE_CODE);
    }

    @Override
    public Result<?> sendResetPhoneSms(String phoneNumber) {
        String smsCode = CodeUtil.createSmsCode();
        // TODO:暂时使用一样的短信模板
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setSignName("阿里云短信测试")
                .setTemplateCode("SMS_154950909")
                .setPhoneNumbers(phoneNumber)
                .setTemplateParam("{\"code\":\"" + smsCode + "\"}");
        return sendSms(sendSmsRequest, phoneNumber, smsCode, RedisConstant.REDIS_KEY_SMS_RESET_PASSWORD_PHONE_CODE);
    }

    @Override
    public Result<?> sendRegisterPhoneSms(String phoneNumber) {
        String smsCode = CodeUtil.createSmsCode();
        // TODO:暂时使用一样的短信模板
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setSignName("阿里云短信测试")
                .setTemplateCode("SMS_154950909")
                .setPhoneNumbers(phoneNumber)
                .setTemplateParam("{\"code\":\"" + smsCode + "\"}");
        return sendSms(sendSmsRequest, phoneNumber, smsCode, RedisConstant.REDIS_KEY_SMS_REGISTER_PHONE_CODE);
    }

    @Override
    public Result<?> sendLoginPhoneSms(String phoneNumber) {
        String smsCode = CodeUtil.createSmsCode();
        // TODO:暂时使用一样的短信模板
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setSignName("阿里云短信测试")
                .setTemplateCode("SMS_154950909")
                .setPhoneNumbers(phoneNumber)
                .setTemplateParam("{\"code\":\"" + smsCode + "\"}");
        return sendSms(sendSmsRequest, phoneNumber, smsCode, RedisConstant.REDIS_KEY_SMS_LOGIN_PHONE_CODE);
    }

    @Override
    public Result<Boolean> checkResetSmsCode(String phoneNumber, String smsCode) {
        String redisKey = RedisKey.build(RedisConstant.REDIS_KEY_SMS_RESET_PASSWORD_PHONE_CODE, phoneNumber);
        return ResultUtil.successPost("验证短信验证码成功", smsCode.equals(redisCache.get(redisKey)));
    }

    @Override
    public Result<Boolean> checkBindSmsCode(String phoneNumber, String smsCode) {
        String redisKey = RedisKey.build(RedisConstant.REDIS_KEY_SMS_BIND_PHONE_CODE, phoneNumber);
        return ResultUtil.successPost("验证短信验证码成功", smsCode.equals(redisCache.get(redisKey)));
    }

    /**
     * 发送短信
     *
     * @param sendSmsRequest 短信请求
     * @param phoneNumber    手机号
     * @param smsCode        短信验证码
     * @param prefix         redis前缀
     * @return Result<?> 返回类型
     */
    private Result<?> sendSms(SendSmsRequest sendSmsRequest, String phoneNumber, String smsCode, String prefix) {
        try {
            long expire = redisCache.getExpire(RedisKey.build(prefix, phoneNumber));
            if (expire > 60 * 4) {
                throw new BusinessException(ExceptionMsgEnum.SMS_CODE_SEND_FREQUENTLY);
            }
            smsClient.sendSms(sendSmsRequest);
            redisCache.set(RedisKey.build(prefix, phoneNumber), smsCode, 60 * 5);
            return ResultUtil.successGet("发送短信成功", null);
        } catch (Exception e) {
            throw new SystemException(ExceptionMsgEnum.ALIYUN_SMS_SEND_ERROR, e);
        }
    }

}
