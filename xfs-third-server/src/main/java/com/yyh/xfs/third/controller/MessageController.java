package com.yyh.xfs.third.controller;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.common.web.aop.idempotent.Idempotent;
import com.yyh.xfs.third.sevice.AliyunSmsService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyh
 * @date 2023-12-12
 */
@RestController
@RequestMapping("/third")
public class MessageController {

    private final AliyunSmsService aliyunSmsService;

    public MessageController(AliyunSmsService aliyunSmsService) {
        this.aliyunSmsService = aliyunSmsService;
    }


    @GetMapping("/sendBindPhoneSms")
    public Result<?> sendBindPhoneSms(String phoneNumber) {
        if(!StringUtils.hasText(phoneNumber)){
            return ResultUtil.errorGet("手机号不能为空");
        }
        if(!phoneNumber.matches("^1[3-9]\\d{9}$")){
            return ResultUtil.errorGet("手机号格式不正确");
        }
        return aliyunSmsService.sendBindPhoneSms(phoneNumber);
    }
    @GetMapping("/sendResetPhoneSms")
    public Result<?> sendResetPhoneSms(String phoneNumber) {
        if(!StringUtils.hasText(phoneNumber)){
            return ResultUtil.errorGet("手机号不能为空");
        }
        if(!phoneNumber.matches("^1[3-9]\\d{9}$")){
            return ResultUtil.errorGet("手机号格式不正确");
        }
        return aliyunSmsService.sendResetPhoneSms(phoneNumber);
    }
    @GetMapping("/sendRegisterPhoneSms")
    public Result<?> sendRegisterPhoneSms(String phoneNumber) {
        if(!StringUtils.hasText(phoneNumber)){
            return ResultUtil.errorGet("手机号不能为空");
        }
        if(!phoneNumber.matches("^1[3-9]\\d{9}$")){
            return ResultUtil.errorGet("手机号格式不正确");
        }
        return aliyunSmsService.sendRegisterPhoneSms(phoneNumber);
    }

    /**
     * 验证绑定手机号短信验证码
     * @param phoneNumber 手机号
     * @param smsCode 短信验证码
     * @return 验证结果
     */
    @PostMapping("/checkBindSmsCode")
    public Result<Boolean> checkBindSmsCode(String phoneNumber, String smsCode) {
        if(!StringUtils.hasText(phoneNumber)){
            return ResultUtil.errorPost("手机号不能为空");
        }
        if(!StringUtils.hasText(smsCode)){
            return ResultUtil.errorPost("短信验证码不能为空");
        }
        if(!phoneNumber.matches("^1[3-9]\\d{9}$")){
            return ResultUtil.errorPost("手机号格式不正确");
        }
        if(!smsCode.matches("^\\d{6}$")){
            return ResultUtil.errorPost("短信验证码格式不正确");
        }
        return aliyunSmsService.checkBindSmsCode(phoneNumber, smsCode);
    }
    /**
     * 验证重置密码短信验证码
     * @param phoneNumber 手机号
     * @param smsCode 短信验证码
     * @return 验证结果
     */
    @PostMapping("/checkResetSmsCode")
    public Result<Boolean> checkResetSmsCode(String phoneNumber, String smsCode) {
        if(!StringUtils.hasText(phoneNumber)){
            return ResultUtil.errorPost("手机号不能为空");
        }
        if(!StringUtils.hasText(smsCode)){
            return ResultUtil.errorPost("短信验证码不能为空");
        }
        if(!phoneNumber.matches("^1[3-9]\\d{9}$")){
            return ResultUtil.errorPost("手机号格式不正确");
        }
        if(!smsCode.matches("^\\d{6}$")){
            return ResultUtil.errorPost("短信验证码格式不正确");
        }
        return aliyunSmsService.checkResetSmsCode(phoneNumber, smsCode);
    }
}