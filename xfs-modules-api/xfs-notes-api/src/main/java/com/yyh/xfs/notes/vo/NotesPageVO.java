package com.yyh.xfs.notes.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yyh
 * @date 2024-01-26
 */
@Data
public class NotesPageVO implements Serializable {
    private List<NotesVO> list;
    private Integer total;
    private Integer page;
    private Integer pageSize;
}
