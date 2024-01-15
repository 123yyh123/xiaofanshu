package com.yyh.xfs.user.controller;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.utils.FieldValidationUtil;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.user.service.UserService;
import com.yyh.xfs.user.vo.UserVO;
import com.yyh.xfs.user.vo.ViewUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author yyh
 * @date 2023-12-11
 */
@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    /**
     * 重置密码
     * @param phoneNumber 手机号
     * @param password 密码
     * @param smsCode 短信验证码
     * @return 重置结果
     */
    @PostMapping("/resetPassword")
    public Result<?> resetPassword(String phoneNumber, String password, String smsCode) {
        if(!FieldValidationUtil.isPhoneNumber(phoneNumber)){
            return ResultUtil.errorPost("手机号格式不正确");
        }
        if(!FieldValidationUtil.isPassword(password)){
            return ResultUtil.errorPost("密码必须包含数字和字母，长度为6-16位");
        }
        if(!FieldValidationUtil.isSmsCode(smsCode)){
            return ResultUtil.errorPost("验证码格式不正确");
        }
        return userService.resetPassword(phoneNumber, password, smsCode);
    }
    /**
     * 获取用户信息
     * @param userId 用户id
     * @return 用户信息
     */
    @GetMapping("/getUserInfo")
    public Result<UserVO> getUserInfo(Long userId) {
        log.info("userId:{}",userId);
        return userService.getUserInfo(userId);
    }
    /**
     * 更新用户头像
     * @param userVO 用户信息
     * @return 更新结果
     */
    @PostMapping("/updateAvatarUrl")
    public Result<?> updateAvatarUrl(@RequestBody UserVO userVO) {
        return userService.updateAvatarUrl(userVO);
    }
    /**
     * 更新用户主页背景
     * @param userVO 用户信息
     * @return 更新结果
     */
    @PostMapping("/updateBackgroundImage")
    public Result<?> updateBackgroundImage(@RequestBody UserVO userVO) {
        return userService.updateBackgroundImage(userVO);
    }
    /**
     * 更新用户昵称
     * @param userVO 用户信息
     * @return 用户信息
     */
    @PostMapping("/updateNickname")
    public Result<?> updateNickname(@RequestBody UserVO userVO) {
        return userService.updateNickname(userVO);
    }
    /**
     * 更新用户简介
     * @param userVO 用户信息
     * @return 用户信息
     */
    @PostMapping("/updateIntroduction")
    public Result<?> updateIntroduction(@RequestBody UserVO userVO) {
        return userService.updateIntroduction(userVO);
    }
    /**
     * 更新用户性别
     * @param userVO 用户信息
     * @return 用户信息
     */
    @PostMapping("/updateSex")
    public Result<?> updateSex(@RequestBody UserVO userVO) {
        return userService.updateSex(userVO);
    }
    /**
     * 更新用户生日
     * @param userVO 用户信息
     * @return 用户信息
     */
    @PostMapping("/updateBirthday")
    public Result<Integer> updateBirthday(@RequestBody UserVO userVO) {
        return userService.updateBirthday(userVO);
    }
    /**
     * 更新用户地区
     * @param userVO 用户信息
     * @return 用户信息
     */
    @PostMapping("/updateArea")
    public Result<?> updateArea(@RequestBody UserVO userVO) {
        return userService.updateArea(userVO);
    }

    /**
     * 查看对方用户信息
     * @param userId 用户id
     * @return 用户信息
     */
    @GetMapping("/viewUserInfo")
    public Result<ViewUserVO> viewUserInfo(Long userId) {
        return userService.viewUserInfo(userId);
    }
}
