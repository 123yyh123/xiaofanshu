package com.yyh.xfs.notes.controller;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.notes.domain.NotesCategoryDO;
import com.yyh.xfs.notes.service.NotesCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yyh
 * @date 2024-02-23
 */
@RestController
@RequestMapping("/notes/category")
public class NotesCategoryController {
    private final NotesCategoryService notesCategoryService;

    public NotesCategoryController(NotesCategoryService notesCategoryService) {
        this.notesCategoryService = notesCategoryService;
    }

    @GetMapping("/getNotesCategoryList")
    public Result<List<NotesCategoryDO>> getNotesCategory() {
        return notesCategoryService.getNotesCategory();
    }
}
