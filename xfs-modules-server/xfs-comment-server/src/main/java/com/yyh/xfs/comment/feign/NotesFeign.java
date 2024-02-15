package com.yyh.xfs.comment.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author yyh
 * @date 2024-02-11
 */
@FeignClient("xfs-notes")
public interface NotesFeign {

    @GetMapping("/notes/getNotesBelongUser")
    Long getNotesBelongUser(@RequestParam("notesId") Long notesId);
}
