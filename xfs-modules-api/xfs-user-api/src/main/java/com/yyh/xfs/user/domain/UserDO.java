package com.yyh.xfs.user.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yyh.xfs.user.vo.UserVO;
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
     * qqOpenId，QQ登陆唯一标识，默认为null，即未绑定QQ
     */
    @ApiModelProperty(value = "qqOpenId，QQ登陆唯一标识，默认为null，即未绑定QQ")
    private String qqOpenId;

    /**
     * facebookOpenId，facebook登陆唯一标识，默认为null，即未绑定facebook
     */
    @ApiModelProperty(value = "facebookOpenId，facebook登陆唯一标识，默认为null，即未绑定facebook")
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

    public static UserDO voToDO(UserVO userVO) {
        UserDO userDO = new UserDO();
        userDO.setId(userVO.getId());
        userDO.setNickname(userVO.getNickname());
        userDO.setAge(userVO.getAge());
        userDO.setSex(userVO.getSex());
        userDO.setArea(userVO.getArea());
        userDO.setBirthday(Date.valueOf(userVO.getBirthday()));
        userDO.setHomePageBackground(userVO.getHomePageBackground());
        userDO.setSelfIntroduction(userVO.getSelfIntroduction());
        userDO.setPhoneNumber(userVO.getPhoneNumber());
        userDO.setAvatarUrl(userVO.getAvatarUrl());
        userDO.setUid(userVO.getUid());
        return userDO;
    }
}
