package com.yyh.xfs.job.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyh.xfs.user.domain.UserDO;
import com.yyh.xfs.user.vo.UserVO;

import java.util.List;

/**
 * @author yyh
 * @date 2023-12-23
 */
public interface UserService extends IService<UserDO> {
    /**
     * 根据vo更新用户信息
     * @param userVO vo
     */
    void updateByVos(List<UserVO> userVos);
}
