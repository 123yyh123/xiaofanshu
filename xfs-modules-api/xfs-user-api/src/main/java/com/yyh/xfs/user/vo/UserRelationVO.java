package com.yyh.xfs.user.vo;

import lombok.Data;

/**
 * @author yyh
 * @date 2024-01-15
 * @desc 用户关系视图对象
 */
@Data
public class UserRelationVO {
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户昵称(当有备注时，显示备注)
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 用户简介
     */
    private String selfIntroduction;

    /**
     * 是否为双向关系
     */
    private Boolean bidirectional;
}
