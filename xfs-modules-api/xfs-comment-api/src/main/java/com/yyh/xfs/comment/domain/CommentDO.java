package com.yyh.xfs.comment.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @author yyh
 * @date 2024-02-05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(value = "comment")
public class CommentDO implements Serializable {

    @Id
    private String id;
    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论者id
     */
    private Long commentUserId;

    /**
     * 评论者省份
     */
    private String province;

    /**
     * 评论父级id,如果是一级评论则为0
     */
    private String parentId;

    /**
     * 评论回复者id
     */
    private Long replyUserId;

    /**
     * 评论回复者名称
     */
    private String replyUserName;

    /**
     * 评论图片
     */
    private String pictureUrl;

    /**
     * 评论点赞数
     */
    private Integer commentLikeNum;

    /**
     * 评论所属笔记id
     */
    private Long notesId;

    /**
     * 是否为置顶评论
     */
    private Boolean isTop;

    /**
     * 评论时间
     */
    private Long createTime;
}
