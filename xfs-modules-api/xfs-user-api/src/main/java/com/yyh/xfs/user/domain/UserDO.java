package com.yyh.xfs.user.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.sql.Date;

/**
 * @author yyh
 * @date 2023-12-10
 */
@Data
@ApiModel(value = "用户表")
@TableName("user")
public class UserDO implements Serializable {
/*    CREATE TABLE `user` (
            `id` bigint(20) NOT NULL COMMENT '用户id',
            `uid` varchar(20) COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '账号的唯一凭证，可以为英文，数字，下划线，6-15位组成，相对于用户id，			其不同之处在于用户可以对其进行修改，但是也是唯一的',
            `nickname` varchar(20) COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '昵称',
            `avatar_url` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '用户头像地址',
            `age` tinyint(3) unsigned zerofill NOT NULL COMMENT '年龄',
            `sex` tinyint(1) unsigned zerofill NOT NULL COMMENT '性别，0为女，1为男，默认为0',
            `area` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '地区',
            `self_introduction` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '自我介绍',
            `birthday` date DEFAULT NULL COMMENT '生日',
            `home_page_background` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '主页背景图',
            `occupation` varchar(20) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '职业，可以根据该字段为用户的推荐加一点占比',
            `phone_number` int(11) NOT NULL COMMENT '手机号',
            `password` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT '密码',
            `wx_open_id` varchar(50) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '微信openId，微信登陆唯一标识，默认为null，即未绑定微信',
            `qq_open_id` varchar(50) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT 'QQopenId，QQ登陆唯一标识，默认为null，即未绑定QQ',
            `facebook_open_id` varchar(50) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT 'fackbookopenId，fackbook登陆唯一标识，默认为null，即未绑定facebook',
            `account_status` tinyint(3) unsigned zerofill NOT NULL COMMENT '账号状态，0为正常，1为注销，2为封禁',
            `create__time` timestamp NOT NULL COMMENT '创建时间',
            `update_time` timestamp NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;*/
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    @TableId
    @ApiModelProperty(value = "用户id")
    private Long id;

    /**
     * 账号的唯一凭证，可以为英文，数字，下划线，6-15位组成，相对于用户id，其不同之处在于用户可以对其进行修改，但是也是唯一的
     */
    @ApiModelProperty(value = "账号的唯一凭证，可以为英文，数字，下划线，6-15位组成，相对于用户id，其不同之处在于用户可以对其进行修改，但是也是唯一的")
    private String uid;

    /**
     * 昵称
     */
    @ApiModelProperty(value = "昵称")
    private String nickname;

    /**
     * 用户头像地址
     */
    @ApiModelProperty(value = "用户头像地址")
    private String avatarUrl;

    /**
     * 年龄
     */
    @ApiModelProperty(value = "年龄")
    private Integer age;

    /**
     * 性别，0为女，1为男，默认为0
     */
    @ApiModelProperty(value = "性别，0为女，1为男，2为保密，默认为2")
    private Integer sex;

    /**
     * 地区
     */
    @ApiModelProperty(value = "地区")
    private String area;

    /**
     * 自我介绍
     */
    @ApiModelProperty(value = "自我介绍")
    private String selfIntroduction;

    /**
     * 生日
     */
    @ApiModelProperty(value = "生日")
    private Date birthday;

    /**
     * 主页背景图
     */
    @ApiModelProperty(value = "主页背景图")
    private String homePageBackground;

    /**
     * 职业，可以根据该字段为用户的推荐加一点占比
     */
    @ApiModelProperty(value = "职业，可以根据该字段为用户的推荐加一点占比")
    private String occupation;

    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号")
    private String phoneNumber;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    private String password;

    /**
     * 微信openId，微信登陆唯一标识，默认为null，即未绑定微信
     */
    @ApiModelProperty(value = "微信openId，微信登陆唯一标识，默认为null，即未绑定微信")
    private String wxOpenId;

    /**
     * QQopenId，QQ登陆唯一标识，默认为null，即未绑定QQ
     */
    @ApiModelProperty(value = "QQopenId，QQ登陆唯一标识，默认为null，即未绑定QQ")
    private String qqOpenId;

    /**
     * fackbookOpenId，fackbook登陆唯一标识，默认为null，即未绑定facebook
     */
    @ApiModelProperty(value = "fackbookOpenId，fackbook登陆唯一标识，默认为null，即未绑定facebook")
    private String facebookOpenId;

    /**
     * 账号状态，0为正常，1为注销，2为封禁
     */
    @ApiModelProperty(value = "账号状态，0为正常，1为注销，2为封禁")
    private Integer accountStatus;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private java.util.Date createTime;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private java.util.Date updateTime;

}
