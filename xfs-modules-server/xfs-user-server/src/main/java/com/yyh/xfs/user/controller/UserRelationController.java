package com.yyh.xfs.user.controller;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.user.service.UserRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyh
 * @date 2024-01-15
 */
@RestController
@RequestMapping("/user/relation")
public class UserRelationController {

    private final UserRelationService userRelationService;

    public UserRelationController(UserRelationService userRelationService) {
        this.userRelationService = userRelationService;
    }

    @GetMapping("/isBlack")
    public Result<Boolean> selectOneByUserIdAndBlackIdIsExist(Long toId, Long fromId) {
        return userRelationService.selectOneByUserIdAndBlackIdIsExist(toId, fromId);
    }

    @GetMapping("/isAttention")
    public Result<Boolean> selectOneByUserIdAndAttentionIdIsExist(Long toId, Long fromId) {
        return userRelationService.selectOneByUserIdAndAttentionIdIsExist(toId, fromId);
    }
}
