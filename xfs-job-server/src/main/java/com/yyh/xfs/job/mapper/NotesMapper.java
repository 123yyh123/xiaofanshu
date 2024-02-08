package com.yyh.xfs.job.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author yyh
 * @date 2024-02-08
 */
@Mapper
@DS("xfs_notes")
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
    boolean updateNotesCollectNum(Long notesId, Integer collectNum);
}
