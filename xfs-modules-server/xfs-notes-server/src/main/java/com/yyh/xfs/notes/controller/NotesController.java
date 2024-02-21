package com.yyh.xfs.notes.controller;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.web.exception.BusinessException;
import com.yyh.xfs.notes.domain.NotesDO;
import com.yyh.xfs.notes.service.NotesService;
import com.yyh.xfs.notes.vo.NotesPageVO;
import com.yyh.xfs.notes.vo.NotesPublishVO;
import com.yyh.xfs.notes.vo.NotesVO;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

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

    /**
     * 查看笔记
     *
     * @param notesId 笔记id
     * @return 笔记内容
     */
    @PostMapping("/viewNotes")
    public Result<?> viewNotes(Long notesId) {
        if (notesId == null) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return notesService.viewNotes(notesId);
    }
    /**
     * 获取笔记所属用户
     *
     * @param notesId 笔记id
     * @return 笔记所属用户id
     */
    @GetMapping("/getNotesBelongUser")
    public Long getNotesBelongUser(@RequestParam("notesId") Long notesId) {
        NotesDO notesDO = notesService.getById(notesId);
        return notesDO.getBelongUserId();
    }

    /**
     * 更新笔记
     * @param notesPublishVO 笔记信息
     * @return 更新结果
     */
    @PutMapping("/updateNotes")
    public Result<?> updateNotes(@RequestBody NotesPublishVO notesPublishVO) {
        if(Objects.isNull(notesPublishVO) || Objects.isNull(notesPublishVO.getNotesId())) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return notesService.updateNotes(notesPublishVO);
    }

    /**
     * 删除笔记
     * @param notesId 笔记id
     * @return 删除结果
     */
    @DeleteMapping("/deleteNotes")
    public Result<?> deleteNotes(@RequestParam("notesId") Long notesId) {
        if(Objects.isNull(notesId)) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return notesService.deleteNotes(notesId);
    }

    /**
     * 更改笔记公开状态
     * @param notesId 笔记id
     * @param authority 公开状态
     * @return 更改结果
     */
    @PostMapping("/changeNotesAuthority")
    public Result<?> changeNotesAuthority(@RequestParam("notesId") Long notesId,@RequestParam("authority") Integer authority) {
        if(Objects.isNull(notesId) || Objects.isNull(authority)) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return notesService.changeNotesAuthority(notesId,authority);
    }

    /**
     * 获取笔记总数和点赞总数和收藏总数
     * @return 笔记总数和点赞总数和收藏总数
     */
    @GetMapping("/getAllNotesCountAndPraiseCountAndCollectCount")
    public Result<Map<String,Integer>> getAllNotesCountAndPraiseCountAndCollectCount() {
        return notesService.getAllNotesCountAndPraiseCountAndCollectCount();
    }

}
