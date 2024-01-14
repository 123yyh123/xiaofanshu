package com.yyh.xfs.user.service.impl;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.user.mapper.UserAttentionMapper;
import com.yyh.xfs.user.mapper.UserBlackMapper;
import com.yyh.xfs.user.mapper.UserFansMapper;
import com.yyh.xfs.user.service.UserRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author yyh
 * @date 2024-01-15
 */
@Service
public class UserRelationServiceImpl implements UserRelationService {
    private final UserAttentionMapper userAttentionMapper;
    private final UserBlackMapper userBlackMapper;
    private final UserFansMapper userFansMapper;

    public UserRelationServiceImpl(UserAttentionMapper userAttentionMapper, UserBlackMapper userBlackMapper, UserFansMapper userFansMapper) {
        this.userAttentionMapper = userAttentionMapper;
        this.userBlackMapper = userBlackMapper;
        this.userFansMapper = userFansMapper;
    }

    @Override
    public Result<Boolean> selectOneByUserIdAndBlackIdIsExist(Long toId, Long fromId) {
        return ResultUtil.successGet(userBlackMapper.selectOneByUserIdAndBlackIdIsExist(toId, fromId));
    }

    @Override
    public Result<Boolean> selectOneByUserIdAndAttentionIdIsExist(Long toId, Long fromId) {
        return ResultUtil.successGet(userAttentionMapper.selectOneByUserIdAndAttentionIdIsExist(toId, fromId));
    }
}
