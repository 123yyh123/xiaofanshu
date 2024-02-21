package com.yyh.xfs.user.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author yyh
 * @date 2024-02-21
 */
@Getter
@Setter
public class PasswordVO implements Serializable {
    private String oldPassword;
    private String newPassword;
}
