package com.yyh.xfs.comment.service;

import com.yyh.xfs.comment.domain.CommentDO;
import com.yyh.xfs.comment.vo.CommentVO;
import com.yyh.xfs.common.domain.Result;

import java.util.List;

/**
 * @author yyh
 * @date 2024-02-05
 */
public interface CommentService {
    Result<CommentVO> addComment(CommentDO commentDO);

    Result<Integer> getCommentCount(Long notesId);

    Result<List<CommentVO>> getCommentFirstList(Long notesId, Integer page, Integer pageSize);

    Result<List<CommentVO>> getCommentSecondList(Long notesId,String parentId, Integer page, Integer pageSize);

    Result<Boolean> praiseComment(String commentId, Long userId, Long targetUserId);
}
