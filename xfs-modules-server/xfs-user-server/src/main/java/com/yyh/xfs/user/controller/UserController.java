package com.yyh.xfs.user.controller;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.utils.FieldValidationUtil;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.user.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyh
 * @date 2023-12-11
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
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
}
