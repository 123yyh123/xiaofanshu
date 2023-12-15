package com.yyh.xfs.user.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.user.domain.UserDO;
import com.yyh.xfs.user.vo.RegisterInfoVO;
import com.yyh.xfs.user.vo.UserVO;

/**
* @author 86131
* @description 针对表【user】的数据库操作Service
* @createDate 2023-12-11 15:26:32
*/
public interface UserService extends IService<UserDO> {

    Result<UserVO> login(String phoneNumber, String password);

    Result<UserVO> otherLogin(Integer type, String code);

    Result<UserVO> bindPhone(RegisterInfoVO registerInfoVO);

    Result<?> resetPassword(String phoneNumber, String password, String smsCode);

    Result<?> register(RegisterInfoVO registerInfoVO);
}
