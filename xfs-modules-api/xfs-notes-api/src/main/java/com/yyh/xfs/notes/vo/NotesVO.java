package com.yyh.xfs.notes.vo;

import com.yyh.xfs.notes.dto.ResourcesDTO;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @author yyh
 * @date 2024-01-26
 */
@Data
public class NotesVO implements Serializable {
    private Long id;
    private String title;
    private String coverPicture;
    private String nickname;
    private String avatarUrl;
    private Integer notesLikeNum;
    private Integer notesViewNum;
    private Integer notesType;
    private Boolean isLike;
    private Boolean isCollect;
    private Boolean isFollow;
    private List<ResourcesDTO> notesResources;
}
