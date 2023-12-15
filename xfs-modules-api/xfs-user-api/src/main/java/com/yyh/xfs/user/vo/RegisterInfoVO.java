package com.yyh.xfs.user.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author yyh
 * @date 2023-12-12
 */
@Getter
@Setter
public class RegisterInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String phoneNumber;
    private String password;
    private String smsCode;
    private Integer registerType;
    private String openId;
    private String nickname;
    private String avatarUrl;
}
