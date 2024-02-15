package com.yyh.xfs.user.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.user.domain.UserDO;
import com.yyh.xfs.user.vo.RegisterInfoVO;
import com.yyh.xfs.user.vo.UserTrtcVO;
import com.yyh.xfs.user.vo.UserVO;
import com.yyh.xfs.user.vo.ViewUserVO;

/**
 * @author yyh
 * @date 2023-12-11
 * 用户服务
*/
public interface UserService extends IService<UserDO> {
    /**
     * 手机号登录
     * @param phoneNumber 手机号
     * @param password 密码
     * @return 登录结果
     */
    Result<UserVO> login(String phoneNumber, String password);
    /**
     * 第三方登录
     * @param type 登录类型
     * @param code 第三方登录凭证
     * @return 登录结果
     */
    Result<UserVO> otherLogin(Integer type, String code);
    /**
     * 绑定手机号
     * @param registerInfoVO 注册信息
     * @return 绑定结果
     */
    Result<UserVO> bindPhone(RegisterInfoVO registerInfoVO);
    /**
     * 重置密码
     * @param phoneNumber 手机号
     * @param password 密码
     * @param smsCode 短信验证码
     * @return 重置结果
     */
    Result<?> resetPassword(String phoneNumber, String password, String smsCode);
    /**
     * 手机号注册
     * @param registerInfoVO 注册信息
     * @return 注册结果
     */
    Result<?> register(RegisterInfoVO registerInfoVO);
    /**
     * 获取用户信息
     * @param userId 用户id
     * @return 用户信息
     */
    Result<UserVO> getUserInfo(Long userId);
    /**
     * 更新用户头像
     * @param userVO 用户信息
     * @return 更新结果
     */
    Result<?> updateAvatarUrl(UserVO userVO);
    /**
     * 更新用户主页背景图
     * @param userVO 用户信息
     * @return 更新结果
     */
    Result<?> updateBackgroundImage(UserVO userVO);
    /**
     * 更新昵称
     * @param userVO 用户信息
     * @return 更新结果
     */
    Result<?> updateNickname(UserVO userVO);
    /**
     * 更新简介
     * @param userVO 用户信息
     * @return 更新结果
     */
    Result<?> updateIntroduction(UserVO userVO);
    /**
     * 更新性别
     * @param userVO 用户信息
     * @return 更新结果
     */
    Result<?> updateSex(UserVO userVO);
    /**
     * 更新生日
     * @param userVO 用户信息
     * @return 更新结果
     */
    Result<Integer> updateBirthday(UserVO userVO);
    /**
     * 更新地区
     * @param userVO 用户信息
     * @return 更新结果
     */
    Result<?> updateArea(UserVO userVO);

    Result<ViewUserVO> viewUserInfo(Long userId);

    Result<?> logout(Long userId);
}
