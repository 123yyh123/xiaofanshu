package com.yyh.xfs.third.sevice;

import com.yyh.xfs.common.domain.Result;

/**
 * @author yyh
 * @date 2023-12-15
 */
public interface AliyunSmsService {
    Result<?> sendBindPhoneSms(String phoneNumber);

    Result<?> sendResetPhoneSms(String phoneNumber);

    Result<?> sendRegisterPhoneSms(String phoneNumber);

    Result<Boolean> checkResetSmsCode(String phoneNumber, String smsCode);

    Result<Boolean> checkBindSmsCode(String phoneNumber, String smsCode);

    Result<?> sendLoginPhoneSms(String phoneNumber);
}
