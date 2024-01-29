package com.yyh.xfs.search.controller;

import com.yyh.xfs.common.domain.PageParam;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.web.exception.BusinessException;
import com.yyh.xfs.notes.vo.NotesPageVO;
import com.yyh.xfs.search.service.NotesSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/getNotesNearBy")
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

}
