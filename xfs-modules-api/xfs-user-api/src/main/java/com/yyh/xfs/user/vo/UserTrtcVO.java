package com.yyh.xfs.user.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author yyh
 * @date 2024-01-13
 */
@Data
public class UserTrtcVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userId;
    private long sdkAppId;
    private String userSig;
}
