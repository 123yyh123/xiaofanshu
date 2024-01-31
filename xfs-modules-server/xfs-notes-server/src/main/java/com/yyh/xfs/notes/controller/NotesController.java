package com.yyh.xfs.notes.controller;

import com.yyh.xfs.common.domain.PageParam;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.web.exception.BusinessException;
import com.yyh.xfs.notes.service.NotesService;
import com.yyh.xfs.notes.vo.NotesPageVO;
import com.yyh.xfs.notes.vo.NotesPublishVO;
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
}
