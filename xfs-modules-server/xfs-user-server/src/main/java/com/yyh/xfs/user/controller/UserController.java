package com.yyh.xfs.user.controller;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.redis.constant.BloomFilterMap;
import com.yyh.xfs.common.utils.FieldValidationUtil;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.common.web.aop.bloomFilter.BloomFilterProcessing;
import com.yyh.xfs.common.web.aop.idempotent.Idempotent;
import com.yyh.xfs.user.service.UserService;
import com.yyh.xfs.user.vo.PasswordVO;
import com.yyh.xfs.user.vo.UserBindThirdStateVO;
import com.yyh.xfs.user.vo.UserVO;
import com.yyh.xfs.user.vo.ViewUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    @Idempotent(value = "/user/resetPassword", expireTime = 60000)
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
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userId"})
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
    @Idempotent(value = "/user/updateAvatarUrl", expireTime = 3000)
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userVO.id"})
    public Result<?> updateAvatarUrl(@RequestBody UserVO userVO) {
        return userService.updateAvatarUrl(userVO);
    }
    /**
     * 更新用户主页背景
     * @param userVO 用户信息
     * @return 更新结果
     */
    @PostMapping("/updateBackgroundImage")
    @Idempotent(value = "/user/updateBackgroundImage", expireTime = 3000)
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userVO.id"})
    public Result<?> updateBackgroundImage(@RequestBody UserVO userVO) {
        return userService.updateBackgroundImage(userVO);
    }
    /**
     * 更新用户昵称
     * @param userVO 用户信息
     * @return 用户信息
     */
    @PostMapping("/updateNickname")
    @Idempotent(value = "/user/updateNickname", expireTime = 3000)
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userVO.id"})
    public Result<?> updateNickname(@RequestBody UserVO userVO) {
        return userService.updateNickname(userVO);
    }
    /**
     * 更新用户简介
     * @param userVO 用户信息
     * @return 用户信息
     */
    @PostMapping("/updateIntroduction")
    @Idempotent(value = "/user/updateIntroduction", expireTime = 3000)
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userVO.id"})
    public Result<?> updateIntroduction(@RequestBody UserVO userVO) {
        return userService.updateIntroduction(userVO);
    }
    /**
     * 更新用户性别
     * @param userVO 用户信息
     * @return 用户信息
     */
    @PostMapping("/updateSex")
    @Idempotent(value = "/user/updateSex", expireTime = 3000)
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userVO.id"})
    public Result<?> updateSex(@RequestBody UserVO userVO) {
        return userService.updateSex(userVO);
    }
    /**
     * 更新用户生日
     * @param userVO 用户信息
     * @return 用户信息
     */
    @PostMapping("/updateBirthday")
    @Idempotent(value = "/user/updateBirthday", expireTime = 3000)
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userVO.id"})
    public Result<Integer> updateBirthday(@RequestBody UserVO userVO) {
        return userService.updateBirthday(userVO);
    }
    /**
     * 更新用户地区
     * @param userVO 用户信息
     * @return 用户信息
     */
    @PostMapping("/updateArea")
    @Idempotent(value = "/user/updateArea", expireTime = 3000)
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userVO.id"})
    public Result<?> updateArea(@RequestBody UserVO userVO) {
        return userService.updateArea(userVO);
    }

    /**
     * 查看对方用户信息
     * @param userId 用户id
     * @return 用户信息
     */
    @GetMapping("/viewUserInfo")
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userId"})
    public Result<ViewUserVO> viewUserInfo(Long userId) {
        return userService.viewUserInfo(userId);
    }

    /**
     * 获取用户是否绑定第三方
     * @return 用户绑定状态
     */
    @GetMapping("/getUserIsBindThird")
    public Result<UserBindThirdStateVO> getUserIsBindThird() {
        return userService.getUserIsBindThird();
    }

    /**
     * 更新手机号
     * @param phoneNumber 手机号
     * @param smsCode 短信验证码
     * @return 更新结果
     */
    @PostMapping("/updatePhoneNumber")
    @Idempotent(value = "/user/updatePhoneNumber", expireTime = 60000)
    public Result<Boolean> updatePhoneNumber(String phoneNumber, String smsCode) {
        if(!FieldValidationUtil.isPhoneNumber(phoneNumber)){
            return ResultUtil.errorPost("手机号格式不正确");
        }
        if(!FieldValidationUtil.isSmsCode(smsCode)){
            return ResultUtil.errorPost("验证码格式不正确");
        }
        return userService.updatePhoneNumber(phoneNumber, smsCode);
    }

    /**
     * 通过旧密码重置密码
     * @param passwordVO 密码信息
     * @return 重置结果
     */
    @PostMapping("/resetPasswordByOld")
    @Idempotent(value = "/user/resetPasswordByOld", expireTime = 60000)
    public Result<?> resetPasswordByOld(@RequestBody PasswordVO passwordVO) {
        return userService.resetPasswordByOld(passwordVO);
    }
}
