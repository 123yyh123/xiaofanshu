package com.yyh.xfs.user.controller;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.user.service.UserRelationService;
import com.yyh.xfs.user.vo.UserRelationVO;
import com.yyh.xfs.user.vo.ViewUserVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 查询是否存在黑名单
     * @param toId 拉黑的用户id
     * @param fromId 被拉黑的用户id
     * @return 是否存在黑名单
     */
    @GetMapping("/isBlack")
    public Result<Boolean> selectOneByUserIdAndBlackIdIsExist(Long toId, Long fromId) {
        return userRelationService.selectOneByUserIdAndBlackIdIsExist(toId, fromId);
    }
    /**
     * 查询是否存在关注
     * @param toId 关注的用户id
     * @param fromId 被关注的用户id
     * @return 是否存在关注
     */
    @GetMapping("/isAttention")
    public Result<Boolean> selectOneByUserIdAndAttentionIdIsExist(Long toId, Long fromId) {
        return userRelationService.selectOneByUserIdAndAttentionIdIsExist(toId, fromId);
    }

    @GetMapping("/attentionList")
    public Result<List<UserRelationVO>> selectAttentionList(Long userId, Integer pageNum, Integer pageSize) {
        return userRelationService.selectAttentionList(userId,pageNum,pageSize);
    }

    @GetMapping("/fansList")
    public Result<List<UserRelationVO>> selectFansList(Long userId, Integer pageNum, Integer pageSize) {
        return userRelationService.selectFansList(userId,pageNum,pageSize);
    }

    @PostMapping("/attention")
    public Result<Boolean> attention(Long userId, Long targetUserId) {
        return userRelationService.attention(userId,targetUserId);
    }

    @PostMapping("/updateRemarkName")
    public Result<?> updateRemarkName(Long userId, Long targetUserId, String remarkName) {
        return userRelationService.updateRemarkName(userId,targetUserId,remarkName);
    }
}
