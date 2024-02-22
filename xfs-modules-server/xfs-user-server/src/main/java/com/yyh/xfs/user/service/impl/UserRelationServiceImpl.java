package com.yyh.xfs.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.redis.constant.RedisConstant;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.common.redis.utils.RedisKey;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.common.web.exception.BusinessException;
import com.yyh.xfs.user.domain.UserAttentionDO;
import com.yyh.xfs.user.domain.UserFansDO;
import com.yyh.xfs.user.mapper.UserAttentionMapper;
import com.yyh.xfs.user.mapper.UserBlackMapper;
import com.yyh.xfs.user.mapper.UserFansMapper;
import com.yyh.xfs.user.service.UserRelationService;
import com.yyh.xfs.user.vo.UserRelationVO;
import com.yyh.xfs.user.vo.ViewUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author yyh
 * @date 2024-01-15
 */
@Service
public class UserRelationServiceImpl implements UserRelationService {
    private final UserAttentionMapper userAttentionMapper;
    private final UserBlackMapper userBlackMapper;
    private final UserFansMapper userFansMapper;
    private final RedisCache redisCache;

    public UserRelationServiceImpl(UserAttentionMapper userAttentionMapper, UserBlackMapper userBlackMapper, UserFansMapper userFansMapper, RedisCache redisCache) {
        this.userAttentionMapper = userAttentionMapper;
        this.userBlackMapper = userBlackMapper;
        this.userFansMapper = userFansMapper;
        this.redisCache = redisCache;
    }

    @Override
    public Result<Boolean> selectOneByUserIdAndBlackIdIsExist(Long toId, Long fromId) {
        return ResultUtil.successGet(userBlackMapper.selectOneByUserIdAndBlackIdIsExist(toId, fromId));
    }

    @Override
    public Result<Boolean> selectOneByUserIdAndAttentionIdIsExist(Long toId, Long fromId) {
        return ResultUtil.successGet(userAttentionMapper.selectOneByUserIdAndAttentionIdIsExist(toId, fromId));
    }

    @Override
    public Result<List<UserRelationVO>> selectAttentionList(Long userId, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        if(userId == null){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        Integer offset = (pageNum - 1) * pageSize;
        List<UserRelationVO> userRelationVOList = userAttentionMapper.selectAttentionList(userId, offset, pageSize);
        return ResultUtil.successGet(userRelationVOList);
    }

    @Override
    public Result<List<UserRelationVO>> selectFansList(Long userId, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        if(userId == null){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        Integer offset = (pageNum - 1) * pageSize;
        List<UserRelationVO> userRelationVOList = userFansMapper.selectFansList(userId, offset, pageSize);
        return ResultUtil.successGet(userRelationVOList);
    }

    @Override
    @Transactional
    public Result<Boolean> attention(Long userId, Long targetUserId) {
        if(userId == null || targetUserId == null){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if(userId.equals(targetUserId)){
            return ResultUtil.errorPost("不能关注自己");
        }
        UserAttentionDO userAttentionDO=userAttentionMapper.getExist(userId,targetUserId);
        if(Objects.nonNull(userAttentionDO)){
            userAttentionMapper.deleteById(userAttentionDO.getId());
            userFansMapper.delete(new QueryWrapper<UserFansDO>().lambda().eq(UserFansDO::getUserId,targetUserId).eq(UserFansDO::getFansId,userId));
            redisCache.hincr(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(userId)), "attentionNum", -1);
            redisCache.hincr(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(targetUserId)), "fansNum", -1);
        }else {
            userAttentionDO=new UserAttentionDO();
            userAttentionDO.setUserId(userId);
            userAttentionDO.setAttentionId(targetUserId);
            userAttentionDO.setCreateTime(new Date());
            userAttentionMapper.insert(userAttentionDO);
            UserFansDO userFansDO=new UserFansDO();
            userFansDO.setUserId(targetUserId);
            userFansDO.setFansId(userId);
            userFansDO.setCreateTime(new Date());
            userFansMapper.insert(userFansDO);
            redisCache.hincr(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(userId)), "attentionNum", 1);
            redisCache.hincr(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(targetUserId)), "fansNum", 1);
        }
        // 更新redis
        redisCache.del(RedisKey.build(RedisConstant.REDIS_KEY_USER_RELATION_ALLOW_SEND_MESSAGE,targetUserId+":"+userId));
        return ResultUtil.successPost(true);
    }

    @Override
    public Result<?> updateRemarkName(Long userId, Long targetUserId, String remarkName) {
        if(userId == null || targetUserId == null){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        UserAttentionDO userAttentionDO=userAttentionMapper.getExist(userId,targetUserId);
        if(Objects.isNull(userAttentionDO)){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        userAttentionDO.setRemarkName(remarkName);
        userAttentionMapper.updateById(userAttentionDO);
        return ResultUtil.successPost(null);
    }

    @Override
    public Result<List<Long>> getAttentionUserId(Long userId) {
        List<Long> list= userAttentionMapper.getAttentionUserId(userId);
        return ResultUtil.successGet(list);
    }
}
