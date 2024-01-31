package com.yyh.xfs.notes.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyh.xfs.common.domain.PageParam;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.notes.domain.NotesDO;
import com.yyh.xfs.notes.vo.NotesPageVO;
import com.yyh.xfs.notes.vo.NotesPublishVO;

/**
 * @author yyh
 * @date 2024-01-19
 */
public interface NotesService extends IService<NotesDO> {
    Result<?> addNotes(NotesPublishVO notesPublishVO);

    Result<NotesPageVO> getLastNotesByPage(Integer page, Integer pageSize);

    Result<?> initNotesLike(Long notesId);

    Result<?> initNotesCollect(Long notesId);

    Result<NotesPageVO> getNotesByUserId(Integer page, Integer pageSize, Integer authority, Integer type);
}
