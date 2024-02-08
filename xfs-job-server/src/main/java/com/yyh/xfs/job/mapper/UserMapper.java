package com.yyh.xfs.job.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyh.xfs.user.domain.UserDO;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yyh
 * @date 2023-12-09
 * @desc 用户表
*/
@Mapper
@DS("xfs_user")
public interface UserMapper extends BaseMapper<UserDO> {

}




