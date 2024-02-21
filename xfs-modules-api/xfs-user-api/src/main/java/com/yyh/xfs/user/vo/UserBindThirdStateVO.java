package com.yyh.xfs.user.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author yyh
 * @date 2024-02-20
 */
@Getter
@Setter
public class UserBindThirdStateVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Boolean wechatBind;
    private Boolean qqBind;
    private Boolean facebookBind;
}
