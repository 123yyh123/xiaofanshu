package com.yyh.xfs.search.controller;

import com.yyh.xfs.common.domain.PageParam;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.web.exception.BusinessException;
import com.yyh.xfs.notes.vo.NotesPageVO;
import com.yyh.xfs.search.service.NotesSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author yyh
 * @date 2024-01-24
 */
@RestController
@RequestMapping("/search/notes")
public class NotesSearchController {

    private final NotesSearchService notesSearchService;

    public NotesSearchController(NotesSearchService notesSearchService) {
        this.notesSearchService = notesSearchService;
    }

    @PostMapping("/getNotesNearBy")
    public Result<NotesPageVO> getNotesNearBy(@RequestBody PageParam pageParam) {
        if (pageParam.getPage() == null || pageParam.getPageSize() == null) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if (pageParam.getLatitude() == null || pageParam.getLongitude() == null) {
            throw new BusinessException(ExceptionMsgEnum.GET_GEOGRAPHIC_INFORMATION_ERROR);
        }
        if (pageParam.getPage() < 1) {
            pageParam.setPage(1);
        }
        if (pageParam.getPageSize() < 1) {
            pageParam.setPageSize(10);
        }
        return notesSearchService.getNotesNearBy(pageParam);
    }

    @GetMapping("/getNotesByKeyword")
    public Result<NotesPageVO> getNotesByKeyword(String keyword, Integer page, Integer pageSize,Integer notesType,Integer hot){
        if (page == null || pageSize == null) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        // 0：图片笔记，1：视频笔记，2：全部
        if (notesType == null||notesType < 0||notesType > 2) {
            notesType = 2;
        }
        // 0：最新，1：最热，2：全部
        if (hot == null||hot < 0||hot > 2) {
            hot = 2;
        }
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        return notesSearchService.getNotesByKeyword(keyword, page, pageSize,notesType,hot);
    }

}
