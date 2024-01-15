package com.yyh.xfs.user.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyh.xfs.user.domain.UserAttentionDO;
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
public interface UserAttentionMapper extends BaseMapper<UserAttentionDO> {
    Boolean selectOneByUserIdAndAttentionIdIsExist(Long userId, Long attentionId);

    @Select("select count(*) from user_attention where user_id=#{userId}")
    Integer getCountById(Long userId);

    List<UserRelationVO> selectAttentionList(Long userId, Integer offset, Integer pageSize);

    @Select("select * from user_attention where user_id=#{userId} and attention_id=#{targetUserId}")
    UserAttentionDO getExist(Long userId, Long targetUserId);
}
