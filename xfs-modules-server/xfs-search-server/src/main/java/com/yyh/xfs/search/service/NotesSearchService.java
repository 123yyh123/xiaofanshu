package com.yyh.xfs.search.service;

import com.yyh.xfs.common.domain.PageParam;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.notes.domain.NotesDO;
import com.yyh.xfs.notes.vo.NotesPageVO;

/**
 * @author yyh
 * @date 2024-01-24
 */
public interface NotesSearchService {
    void addNotes(NotesDO notesDO);

    Result<NotesPageVO> getNotesNearBy(PageParam pageParam);
}
