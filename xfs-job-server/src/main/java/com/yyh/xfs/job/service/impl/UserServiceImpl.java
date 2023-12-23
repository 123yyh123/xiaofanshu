package com.yyh.xfs.job.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.web.exception.BusinessException;
import com.yyh.xfs.common.web.exception.OnlyWarnException;
import com.yyh.xfs.job.mapper.UserMapper;
import com.yyh.xfs.job.service.UserService;
import com.yyh.xfs.user.domain.UserDO;
import com.yyh.xfs.user.vo.UserVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yyh
 * @date 2023-12-23
 */
@Service
@DS("xfs_user")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    /**
     * 没有用updateBatchById，因为该方法有事务，会导致事务回滚，而我们不需要事务回滚
     * @param userVos vo
     */
    @Override
    public void updateByVos(List<UserVO> userVos) {
        if (userVos == null || userVos.isEmpty()) {
            return;
        }
        List<UserDO> users = this.listByIds(userVos.stream().map(UserVO::getId).collect(Collectors.toList()));
        if (users == null || users.isEmpty()) {
            return;
        }
        users.forEach(userDO -> {
            UserVO vo = userVos.stream().filter(userVO1 -> userVO1.getId().equals(userDO.getId())).findFirst().orElse(null);
            if (vo == null) {
                return;
            }
            UserDO user = UserDO.voToDO(userDO, vo);
            try {
                this.updateById(user);
            } catch (Exception e) {
                throw new BusinessException(ExceptionMsgEnum.DB_ERROR);
            }
        });
    }
}
