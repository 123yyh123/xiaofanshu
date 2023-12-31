package com.yyh.xfs.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyh.xfs.user.domain.UserDO;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yyh
 * @date 2023-12-23
 * @desc 用户表
*/
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

}




