package com.yyh.xfs.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyh.xfs.user.domain.UserDO;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 86131
* @description 针对表【user】的数据库操作Mapper
* @createDate 2023-12-11 15:26:32
* @Entity com.yyh.xfs.user.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

}




