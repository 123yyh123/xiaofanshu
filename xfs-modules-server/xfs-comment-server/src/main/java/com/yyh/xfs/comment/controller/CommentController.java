package com.yyh.xfs.comment.controller;

import com.yyh.xfs.comment.domain.CommentDO;
import com.yyh.xfs.comment.service.CommentService;
import com.yyh.xfs.comment.vo.CommentVO;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
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

    @PostMapping("/addComment")
    public Result<CommentVO> addComment(@RequestBody CommentDO commentDO) {
        if (commentDO == null||commentDO.getCommentUserId() == null||commentDO.getNotesId()==null){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if(!StringUtils.hasText(commentDO.getParentId())){
            commentDO.setParentId("0");
        }
        return commentService.addComment(commentDO);
    }

    @GetMapping("/getCommentCount")
    public Result<Integer> getCommentList(Long notesId){
        if (notesId == null){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return commentService.getCommentCount(notesId);
    }

    @GetMapping("/getCommentFirstList")
    public Result<List<CommentVO>> getCommentFirstList(Long notesId, Integer page, Integer pageSize){
        if (notesId == null){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if (page == null){
            page = 1;
        }
        if (pageSize == null){
            pageSize = 10;
        }
        return commentService.getCommentFirstList(notesId, page, pageSize);
    }
}
