package com.yyh.xfs.notes.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author yyh
 * @date 2024-01-22
 */

@Data
@TableName("notes_category")
@AllArgsConstructor
@NoArgsConstructor
public class NotesCategoryDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类id
     */
    @TableId
    private Integer id;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 排序，越大越靠前
     */
    private Integer categorySort;

    /**
     * 图标
     */
    private String icon;
}
