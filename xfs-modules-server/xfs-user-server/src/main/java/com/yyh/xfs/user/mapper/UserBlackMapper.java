package com.yyh.xfs.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyh.xfs.user.domain.UserBlackDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author yyh
 * @date 2023-12-30
 */
@Mapper
public interface UserBlackMapper extends BaseMapper<UserBlackDO> {
    Boolean selectOneByUserIdAndBlackIdIsExist(Long userId, Long blackId);
}
