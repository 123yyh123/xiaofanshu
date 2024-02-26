package com.yyh.xfs.user.controller;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.redis.constant.BloomFilterMap;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.common.web.aop.bloomFilter.BloomFilterProcessing;
import com.yyh.xfs.common.web.aop.idempotent.Idempotent;
import com.yyh.xfs.common.web.exception.BusinessException;
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
    @BloomFilterProcessing(map= BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#toId","#fromId"})
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
    @BloomFilterProcessing(map= BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#toId","#fromId"})
    public Result<Boolean> selectOneByUserIdAndAttentionIdIsExist(Long toId, Long fromId) {
        return userRelationService.selectOneByUserIdAndAttentionIdIsExist(toId, fromId);
    }

    /**
     * 获取用户关注列表
     * @param userId 用户id
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 用户关注列表
     */
    @GetMapping("/attentionList")
    @BloomFilterProcessing(map= BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userId"})
    public Result<List<UserRelationVO>> selectAttentionList(Long userId, Integer pageNum, Integer pageSize) {
        return userRelationService.selectAttentionList(userId,pageNum,pageSize);
    }
    /**
     * 获取用户粉丝列表
     * @param userId 用户id
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 用户粉丝列表
     */
    @GetMapping("/fansList")
    @BloomFilterProcessing(map= BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userId"})
    public Result<List<UserRelationVO>> selectFansList(Long userId, Integer pageNum, Integer pageSize) {
        return userRelationService.selectFansList(userId,pageNum,pageSize);
    }
    /**
     * 关注用户
     * @param userId 用户id
     * @param targetUserId 关注的用户id
     * @return 是否关注成功
     */
    @PostMapping("/attention")
    @Idempotent(value = "/user/relation/attention", expireTime = 500)
    @BloomFilterProcessing(map= BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userId","#targetUserId"})
    public Result<Boolean> attention(Long userId, Long targetUserId) {
        return userRelationService.attention(userId,targetUserId);
    }
    /**
     * 更新备注名
     * @param userId 用户id
     * @param targetUserId 关注的用户id
     * @param remarkName 备注名
     * @return 是否更新成功
     */
    @PostMapping("/updateRemarkName")
    @Idempotent(value = "/user/relation/updateRemarkName", expireTime = 500)
    @BloomFilterProcessing(map= BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userId","#targetUserId"})
    public Result<?> updateRemarkName(Long userId, Long targetUserId, String remarkName) {
        return userRelationService.updateRemarkName(userId,targetUserId,remarkName);
    }

    /**
     * 获取用户关注的用户id
     * @param userId 用户id
     * @return 用户关注的用户id
     */
    @GetMapping("/getAttentionUserId")
    @BloomFilterProcessing(map= BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userId"})
    public Result<List<Long>> getAttentionUserId(@RequestParam("userId") Long userId) {
        if (userId == null) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return userRelationService.getAttentionUserId(userId);
    }
}
