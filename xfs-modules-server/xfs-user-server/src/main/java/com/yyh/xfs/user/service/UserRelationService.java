package com.yyh.xfs.user.service;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.user.vo.UserRelationVO;
import com.yyh.xfs.user.vo.ViewUserVO;

import java.util.List;

/**
 * @author yyh
 * @date 2024-01-15
 */
public interface UserRelationService {
    Result<Boolean> selectOneByUserIdAndBlackIdIsExist(Long toId, Long fromId);

    Result<Boolean> selectOneByUserIdAndAttentionIdIsExist(Long toId, Long fromId);

    Result<List<UserRelationVO>> selectAttentionList(Long userId, Integer pageNum, Integer pageSize);

    Result<List<UserRelationVO>> selectFansList(Long userId, Integer pageNum, Integer pageSize);

    Result<Boolean> attention(Long userId, Long targetUserId);

    Result<?> updateRemarkName(Long userId, Long targetUserId, String remarkName);
}
