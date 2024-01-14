package com.yyh.xfs.user.service;

import com.yyh.xfs.common.domain.Result;

/**
 * @author yyh
 * @date 2024-01-15
 */
public interface UserRelationService {
    Result<Boolean> selectOneByUserIdAndBlackIdIsExist(Long toId, Long fromId);

    Result<Boolean> selectOneByUserIdAndAttentionIdIsExist(Long toId, Long fromId);
}
