package com.yyh.xfs.notes.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yyh
 * @date 2024-01-22
 */
@Data
@TableName("notes_topic_relation")
public class NotesTopicRelationDO implements Serializable {

        private static final long serialVersionUID = 1L;

        @TableId
        private Long id;

        /**
        * 笔记id
        */
        private Long notesId;

        /**
        * 话题id
        */
        private Long topicId;
}
