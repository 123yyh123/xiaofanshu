package com.yyh.xfs.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyh.xfs.im.domain.UserBlackDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author yyh
 * @date 2023-12-30
 */
@Mapper
public interface UserBlackMapper extends BaseMapper<UserBlackDO> {
    Boolean selectOneByUserIdAndBlackIdIsExist(Long userId, Long blackId);
}
