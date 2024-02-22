package com.yyh.xfs.notes.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyh.xfs.common.domain.PageParam;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.notes.domain.NotesDO;
import com.yyh.xfs.notes.vo.NotesPageVO;
import com.yyh.xfs.notes.vo.NotesPublishVO;
import com.yyh.xfs.notes.vo.NotesVO;

import java.util.Map;

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

    Result<NotesVO> getNotesByNotesId(Long notesId);

    Result<?> praiseNotes(Long notesId, Long userId, Long targetUserId);

    Result<?> collectNotes(Long notesId, Long userId, Long targetUserId);

    Result<?> viewNotes(Long notesId);

    Result<?> updateNotes(NotesPublishVO notesPublishVO);

    Result<?> deleteNotes(Long notesId);

    Result<?> changeNotesAuthority(Long notesId, Integer authority);

    Result<Map<String, Integer>> getAllNotesCountAndPraiseCountAndCollectCount();

    Result<NotesPageVO> getNotesByView(Integer page, Integer pageSize, Integer type, Long userId);

    Result<NotesPageVO> getAttentionUserNotes(Integer page, Integer pageSize);
}
