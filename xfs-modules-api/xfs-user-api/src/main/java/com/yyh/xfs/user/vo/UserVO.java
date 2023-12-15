package com.yyh.xfs.user.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author yyh
 * @date 2023-12-11
 */
@Getter
@Setter
public class UserVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String uid;
    private String nickname;
    private String avatarUrl;
    private Integer age;
    private Integer sex;
    private String phoneNumber;
}
