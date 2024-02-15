package com.yyh.xfs.job.mapper.notes;

import com.yyh.xfs.notes.domain.UserCollectNotesDO;
import com.yyh.xfs.notes.domain.UserLikeNotesDO;

/**
 * 笔记mapper
 * @author yyh
 * @date 2023-12-23
 */
public interface NotesMapper {
    /**
     * 更新笔记点赞数
     * @param notesId 笔记id
     * @param likeNum 点赞数
     */
    boolean updateNotesLikeNum(Long notesId, Integer likeNum);

    /**
     * 更新笔记收藏数
     * @param notesId 笔记id
     * @param collectNum 收藏数
     */
    boolean updateNotesCollectionNum(Long notesId, Integer collectNum);

    /**
     * 插入用户点赞笔记
     * @param userLikeNotesDO 用户点赞笔记
     */
    boolean insertUserLikeNotes(UserLikeNotesDO userLikeNotesDO);

    /**
     * 删除用户点赞笔记
     * @param userLikeNotesDO 用户点赞笔记
     */
    void deleteUserLikeNotes(UserLikeNotesDO userLikeNotesDO);

    /**
     * 插入用户收藏笔记
     * @param userCollectNotesDO 用户收藏笔记
     */
    boolean insertUserCollectNotes(UserCollectNotesDO userCollectNotesDO);

    /**
     * 删除用户收藏笔记
     * @param userCollectNotesDO 用户收藏笔记
     */
    void deleteUserCollectNotes(UserCollectNotesDO userCollectNotesDO);

    /**
     * 更新笔记浏览数
     * @param notesId 笔记id
     * @param viewNum 浏览数
     */
    boolean updateNotesViewNum(Long notesId, Integer viewNum);
}
