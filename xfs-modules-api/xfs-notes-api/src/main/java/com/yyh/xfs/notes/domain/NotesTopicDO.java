package com.yyh.xfs.notes.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author yyh
 * @date 2024-01-22
 */
@Data
@TableName("notes_topic")
public class NotesTopicDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 话题id
     */
    @TableId
    private Long id;

    /**
     * 话题名称
     */
    private String topicName;

    /**
     * 话题描述
     */
    private String topicDesc;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 创建人
     */
    private Long createUser;
}
