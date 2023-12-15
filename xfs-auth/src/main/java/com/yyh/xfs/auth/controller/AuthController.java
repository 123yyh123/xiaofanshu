package com.yyh.xfs.auth.controller;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.utils.FieldValidationUtil;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.user.service.UserService;
import com.yyh.xfs.user.vo.RegisterInfoVO;
import com.yyh.xfs.user.vo.UserVO;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyh
 * @date 2023-12-11
 */
@RequestMapping("/auth")
@RestController
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<UserVO> login(String phoneNumber, String password) {
        if(!FieldValidationUtil.isPhoneNumber(phoneNumber)){
            return ResultUtil.errorPost("手机号格式不正确");
        };
        if(!FieldValidationUtil.isPassword(password)){
            return ResultUtil.errorPost("密码必须包含数字和字母，长度为6-16位");
        };
        return userService.login(phoneNumber, password);
    }

    @PostMapping("/otherLogin")
    public Result<UserVO> otherLogin(Integer type,String code) {
        if(type==null){
            return ResultUtil.errorPost("登录类型不能为空");
        }
        if(code==null){
            return ResultUtil.errorPost("code不能为空");
        }
        return userService.otherLogin(type, code);
    }
    @PostMapping("/bindPhone")
    public Result<UserVO> bindPhone(@RequestBody RegisterInfoVO registerInfoVO) {
        if(!FieldValidationUtil.isPhoneNumber(registerInfoVO.getPhoneNumber())){
            return ResultUtil.errorPost("手机号格式不正确");
        }
        if(!FieldValidationUtil.isSmsCode(registerInfoVO.getSmsCode())){
            return ResultUtil.errorPost("验证码格式不正确");
        }
        if(!(registerInfoVO.getRegisterType()==1
                ||registerInfoVO.getRegisterType()==2
                ||registerInfoVO.getRegisterType()==3)){
            return ResultUtil.errorPost("注册类型不正确");
        }
        if(!StringUtils.hasText(registerInfoVO.getOpenId())){
            return ResultUtil.errorPost("openId不能为空");
        }
        return userService.bindPhone(registerInfoVO);
    }
}
