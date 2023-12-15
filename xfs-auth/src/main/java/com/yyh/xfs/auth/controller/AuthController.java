package com.yyh.xfs.auth.controller;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.utils.FieldValidationUtil;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.common.web.exception.BusinessException;
import com.yyh.xfs.user.service.UserService;
import com.yyh.xfs.user.vo.RegisterInfoVO;
import com.yyh.xfs.user.vo.UserVO;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 为检查token是否有效，返回200，因为在网关中已经做了token的校验，所以这里不需要做任何处理
     */
    @GetMapping("/checkToken")
    public Result<?> checkToken() {
        return new Result<>(200,ResultUtil.SUCCESS,null);
    }
    /**
     * 手机号登录
     * @param phoneNumber 手机号
     * @param password 密码
     * @return 登录结果
     */
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
    /**
     * 手机号注册
     * @param registerInfoVO 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterInfoVO registerInfoVO) {
        if(!FieldValidationUtil.isPhoneNumber(registerInfoVO.getPhoneNumber())){
            return ResultUtil.errorPost("手机号格式不正确");
        }
        if(!FieldValidationUtil.isPassword(registerInfoVO.getPassword())){
            return ResultUtil.errorPost("密码必须包含数字和字母，长度为6-16位");
        }
        if(!FieldValidationUtil.isSmsCode(registerInfoVO.getSmsCode())){
            return ResultUtil.errorPost("验证码格式不正确");
        }
        return userService.register(registerInfoVO);
    }
    /**
     * 第三方登录
     * @param type 登录类型
     * @param code 第三方登录凭证
     * @return 登录结果
     */
    @PostMapping("/otherLogin")
    public Result<UserVO> otherLogin(Integer type,String code) {
        if(!(type==1||type==2||type==3)){
            throw new BusinessException(ExceptionMsgEnum.LOGIN_TYPE_ERROR);
        }
        if(!StringUtils.hasText(code)){
            throw new BusinessException(ExceptionMsgEnum.OPEN_ID_NULL);
        }
        return userService.otherLogin(type, code);
    }
    /**
     * 绑定手机号
     * @param registerInfoVO 注册信息
     * @return 绑定结果
     */
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
            throw new BusinessException(ExceptionMsgEnum.LOGIN_TYPE_ERROR);
        }
        if(!StringUtils.hasText(registerInfoVO.getOpenId())){
            throw new BusinessException(ExceptionMsgEnum.OPEN_ID_NULL);
        }
        return userService.bindPhone(registerInfoVO);
    }
}
