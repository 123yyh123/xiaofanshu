package com.yyh.xfs.user.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author yyh
 * @date 2024-02-21
 */
@Data
public class PasswordVO implements Serializable {
    private String oldPassword;
    private String newPassword;
}
