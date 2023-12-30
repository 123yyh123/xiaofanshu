package com.yyh.xfs.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyh.xfs.im.domain.UserAttentionDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author yyh
 * @date 2023-12-30
 */
@Mapper
public interface UserAttentionMapper extends BaseMapper<UserAttentionDO> {
    Boolean selectOneByUserIdAndAttentionIdIsExist(Long userId, Long attentionId);
}
