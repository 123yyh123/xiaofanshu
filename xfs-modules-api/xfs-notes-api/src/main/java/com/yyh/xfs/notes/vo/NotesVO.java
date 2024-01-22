package com.yyh.xfs.notes.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yyh
 * @date 2024-01-22
 */
@Data
public class NotesVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String title;
    private String realContent;
    private String content;
    private Long belongUserId;
    private Integer notesType;
    private String coverPicture;
    private String notesResources;
    private String address;
    private Double longitude;
    private Double latitude;
    private Integer authority;
}
