package com.yyh.xfs.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyh.xfs.user.domain.UserFansDO;
import com.yyh.xfs.user.vo.UserRelationVO;
import com.yyh.xfs.user.vo.ViewUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author yyh
 * @date 2023-12-30
 */
@Mapper
public interface UserFansMapper extends BaseMapper<UserFansDO> {

    @Select("select count(*) from user_fans where user_id=#{userId}")
    Integer getCountById(Long userId);

    List<UserRelationVO> selectFansList(Long userId, Integer offset, Integer pageSize);
}
