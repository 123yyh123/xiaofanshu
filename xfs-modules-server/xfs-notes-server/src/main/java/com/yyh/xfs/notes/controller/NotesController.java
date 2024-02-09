package com.yyh.xfs.notes.controller;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.web.exception.BusinessException;
import com.yyh.xfs.notes.service.NotesService;
import com.yyh.xfs.notes.vo.NotesPageVO;
import com.yyh.xfs.notes.vo.NotesPublishVO;
import com.yyh.xfs.notes.vo.NotesVO;
import org.springframework.web.bind.annotation.*;

/**
 * @author yyh
 * @date 2024-01-19
 */
@RestController
@RequestMapping("/notes")
public class NotesController {

    private final NotesService notesService;

    public NotesController(NotesService notesService) {
        this.notesService = notesService;
    }

    @PostMapping("/publish")
    public Result<?> addNotes(@RequestBody NotesPublishVO notesPublishVO) {
        return notesService.addNotes(notesPublishVO);
    }

    @GetMapping("/getLastNotesByPage")
    public Result<NotesPageVO> getLastNotesByPage(Integer page, Integer pageSize) {
        if (page == null || pageSize == null) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        return notesService.getLastNotesByPage(page, pageSize);
    }

    @GetMapping("/getNotesByUserId")
    public Result<NotesPageVO> getNotesByUserId(Integer page, Integer pageSize,Integer authority,Integer type) {
        if (page == null || pageSize == null) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if(authority == null||authority < 0||authority > 1) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if(type == null||type < 0||type > 2) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        return notesService.getNotesByUserId(page, pageSize,authority,type);
    }

    @GetMapping("/getNotesByNotesId")
    public Result<NotesVO> getNotesByNotesId(Long notesId) {
        if (notesId == null) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return notesService.getNotesByNotesId(notesId);
    }

    /**
     * 点赞笔记
     *
     * @param notesId 笔记id
     * @param userId  用户id
     * @param targetUserId  目标用户id
     * @return 点赞结果
     */
    @PostMapping("/praiseNotes")
    public Result<?> praiseNotes(Long notesId,Long userId,Long targetUserId) {
        if (notesId == null || userId == null||targetUserId == null) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return notesService.praiseNotes(notesId,userId,targetUserId);
    }

    /**
     * 收藏笔记
     *
     * @param notesId 笔记id
     * @param userId  用户id
     * @param targetUserId  目标用户id
     * @return 收藏结果
     */
    @PostMapping("/collectNotes")
    public Result<?> collectNotes(Long notesId,Long userId,Long targetUserId) {
        if (notesId == null || userId == null||targetUserId == null) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return notesService.collectNotes(notesId,userId,targetUserId);
    }
}
