package com.yyh.xfs.notes.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * @author yyh
 * @date 2024-01-18
 */
@Data
@ApiModel(value = "笔记表")
@TableName("notes")
public class NotesDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 笔记id
     */
    @TableId
    private Long id;

    /**
     * 笔记标题
     */
    private String title;

    /**
     * 笔记内容
     */
    private String content;

    /**
     * 所属用户id
     */
    private Long belongUserId;

    /**
     * 所属分类
     */
    private Integer belongCategory;

    /**
     * 笔记类型，0为图片笔记，1为视频笔记
     */
    private Integer notesType;

    /**
     * 封面图片
     */
    private String coverPicture;

    /**
     * 笔记资源，图片或视频，视频只能一个，图片最多9个，格式为json序列化数组后的字符串
     */
    private String notesResources;

    /**
     * 赞数
     */
    private Integer notesLikeNum;

    /**
     * 收藏数
     */
    private Integer notesCollectionNum;

    /**
     * 评论数
     */
    private Integer notesCommentNum;

    /**
     * 展示的地点，可以为空
     */
    private String address;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 是否公开，0为公开，1为私密
     */
    private Integer authority;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
