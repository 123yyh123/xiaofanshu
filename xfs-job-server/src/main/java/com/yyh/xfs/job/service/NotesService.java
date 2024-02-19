package com.yyh.xfs.job.service;

/**
 * @author yyh
 * @date 2024-02-08
 */
public interface NotesService {
    void updateNotesLikeNum(String key,Long notesId, Integer notesLikeNum);

    void updateNotesCollectionNum(String key, Long notesId, Integer notesCollectionNum);

    void updateNotesViewNum(String key, Long notesId, Integer notesViewNum);
}
