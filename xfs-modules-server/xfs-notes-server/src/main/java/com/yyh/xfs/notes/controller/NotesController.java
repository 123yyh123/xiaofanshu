package com.yyh.xfs.notes.controller;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.notes.service.NotesService;
import com.yyh.xfs.notes.vo.NotesVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Result<?> addNotes(@RequestBody NotesVO notesVO) {
        return notesService.addNotes(notesVO);
    }
}
