package com.yyh.xfs.notes.service;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.notes.domain.NotesCategoryDO;

import java.util.List;

/**
 * @author yyh
 * @date 2024-02-23
 */
public interface NotesCategoryService {
    Result<List<NotesCategoryDO>>getNotesCategory();
}
