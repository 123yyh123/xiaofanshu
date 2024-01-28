package com.yyh.xfs.notes.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yyh
 * @date 2024-01-28
 */
@Data
@TableName("user_views_record_notes")
public class UserViewsRecordNotesDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 笔记id
     */
    private Long notesId;
}
