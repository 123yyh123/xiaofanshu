package com.yyh.xfs.comment.controller;

import com.yyh.xfs.comment.domain.CommentDO;
import com.yyh.xfs.comment.service.CommentService;
import com.yyh.xfs.comment.vo.CommentVO;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.redis.constant.BloomFilterMap;
import com.yyh.xfs.common.web.aop.bloomFilter.BloomFilterProcessing;
import com.yyh.xfs.common.web.aop.idempotent.Idempotent;
import com.yyh.xfs.common.web.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yyh
 * @date 2024-02-05
 */
@RestController
@RequestMapping("/comment")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 添加评论
     *
     * @param commentDO 评论信息
     * @return 评论信息
     */
    @PostMapping("/addComment")
    @Idempotent(value = "/comment/addComment", expireTime = 3000)
    public Result<CommentVO> addComment(@RequestBody CommentDO commentDO) {
        if (commentDO == null || commentDO.getCommentUserId() == null || commentDO.getNotesId() == null) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if (!StringUtils.hasText(commentDO.getParentId())) {
            commentDO.setParentId("0");
        }
        return commentService.addComment(commentDO);
    }

    /**
     * 获取一篇笔记的评论数量
     *
     * @param notesId 笔记id
     * @return 评论数量
     */
    @GetMapping("/getCommentCount")
    @BloomFilterProcessing(map = BloomFilterMap.NOTES_ID_BLOOM_FILTER,keys = {"#notesId"})
    public Result<Integer> getCommentList(Long notesId) {
        if (notesId == null) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return commentService.getCommentCount(notesId);
    }

    /**
     * 获取一篇笔记的一级评论列表
     *
     * @param notesId  笔记id
     * @param page     页码
     * @param pageSize 每页数量
     * @return 一级评论列表
     */
    @GetMapping("/getCommentFirstList")
    @BloomFilterProcessing(map = BloomFilterMap.NOTES_ID_BLOOM_FILTER,keys = {"#notesId"})
    public Result<List<CommentVO>> getCommentFirstList(Long notesId, Integer page, Integer pageSize) {
        if (notesId == null) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if (page == null) {
            page = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        return commentService.getCommentFirstList(notesId, page, pageSize);
    }

    /**
     * 获取一级评论的二级评论列表
     *
     * @param notesId  笔记id
     * @param parentId 一级评论id
     * @param page     页码
     * @param pageSize 每页数量
     * @return 二级评论列表
     */
    @GetMapping("/getCommentSecondList")
    @BloomFilterProcessing(map = BloomFilterMap.NOTES_ID_BLOOM_FILTER,keys = {"#notesId"})
    public Result<List<CommentVO>> getCommentSecondList(Long notesId, String parentId, Integer page, Integer pageSize) {
        if (notesId == null || !StringUtils.hasText(parentId)) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if (page == null) {
            page = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        return commentService.getCommentSecondList(notesId, parentId, page, pageSize);
    }

    /**
     * 点赞或取消点赞评论
     *
     * @param commentId 评论id
     * @param userId    用户id
     * @param targetUserId 评论所属用户id
     * @return 是否点赞成功
     */
    @PostMapping("/praiseComment")
    @Idempotent(value = "/comment/praiseComment", expireTime = 500)
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userId","#targetUserId"})
    public Result<Boolean> praiseComment(String commentId, Long userId,Long targetUserId) {
        if (!StringUtils.hasText(commentId) || userId == null) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return commentService.praiseComment(commentId, userId,targetUserId);
    }

    /**
     * 置顶或取消置顶评论
     *
     * @param commentId 评论id
     * @return 是否置顶成功
     */
    @PostMapping("/setTopComment")
    @Idempotent(value = "/comment/setTopComment", expireTime = 3000)
    public Result<Boolean> setTopComment(String commentId) {
        if (!StringUtils.hasText(commentId)) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return commentService.setTopComment(commentId);
    }

    /**
     * 删除评论
     *
     * @param commentId 评论id
     * @return 是否删除成功
     */
    @DeleteMapping("/deleteComment")
    public Result<Boolean> deleteComment(String commentId) {
        if (!StringUtils.hasText(commentId)) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return commentService.deleteComment(commentId);
    }
}
