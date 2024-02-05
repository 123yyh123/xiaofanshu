package com.yyh.xfs.comment.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yyh
 * @date 2024-02-05
 */
@Data
public class CommentVO implements Serializable {
    private String id;
    private String content;
    private String province;
    private Long commentUserId;
    private String parentId;
    private String pictureUrl;
    private Integer commentLikeNum;
    // 评论回复数，只在为一级评论时有效
    private Integer commentReplyNum;
    private Long notesId;
    private Boolean isTop;
    private Boolean isLike;
    private Long createTime;
}
