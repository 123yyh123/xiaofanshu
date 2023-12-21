package com.yyh.xfs.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.redis.constant.RedisConstant;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.common.redis.utils.RedisKey;
import com.yyh.xfs.common.utils.CodeUtil;
import com.yyh.xfs.common.utils.Md5Util;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.common.web.exception.BusinessException;
import com.yyh.xfs.common.web.exception.SystemException;
import com.yyh.xfs.common.web.properties.JwtProperties;
import com.yyh.xfs.common.web.utils.JWTUtil;
import com.yyh.xfs.user.domain.UserDO;
import com.yyh.xfs.user.service.UserService;
import com.yyh.xfs.user.mapper.UserMapper;
import com.yyh.xfs.user.vo.RegisterInfoVO;
import com.yyh.xfs.user.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
* @author 86131
* @description 针对表【user】的数据库操作Service实现
* @createDate 2023-12-11 15:26:32
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService{
    private static final String DEFAULT_NICKNAME_PREFIX = "小番薯用户";

    private final JwtProperties jwtProperties;
    private final RedisCache redisCache;

    public UserServiceImpl(RedisCache redisCache, JwtProperties jwtProperties) {
        this.redisCache = redisCache;
        this.jwtProperties = jwtProperties;
    }
    /**
     * 登录类型和数据库字段的映射
     */
    private static final Map<Integer, SFunction<UserDO, String>> LOGIN_TYPE_MAP = new HashMap<>();

    /**
     * 初始化
     */
    @PostConstruct
    public void postConstruct() {
        LOGIN_TYPE_MAP.put(1, UserDO::getWxOpenId);
        LOGIN_TYPE_MAP.put(2, UserDO::getQqOpenId);
        LOGIN_TYPE_MAP.put(3, UserDO::getFacebookOpenId);
    }
    /**
     * 手机号登录
     * @param phoneNumber 手机号
     * @param password 密码
     * @return UserDO
     */
    @Override
    public Result<UserVO> login(String phoneNumber, String password) {
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
        // 利用MD5加密密码，并且通过手机号给密码加盐
        String md5Password = Md5Util.getMd5(phoneNumber + password);
        queryWrapper.lambda().eq(UserDO::getPhoneNumber, phoneNumber).eq(UserDO::getPassword, md5Password);
        UserDO userDO = this.getOne(queryWrapper);
        if (Objects.isNull(userDO)) {
            throw new BusinessException(ExceptionMsgEnum.PASSWORD_ERROR);
        }
        return generateUserVO(userDO);
    }
/**
     * 第三方登录验证
     * @param type 登录类型
     * @param code 第三方账号的唯一标识
     * @return UserDO
     */
    @Override
    public Result<UserVO> otherLogin(Integer type, String code) {
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(LOGIN_TYPE_MAP.get(type), code);
        UserDO userDO = this.getOne(queryWrapper);
        if (Objects.isNull(userDO)) {
            return ResultUtil.errorPost("该第三方账号未绑定");
        }
        return generateUserVO(userDO);
    }
    /**
     * 第三方登录并绑定手机号
     * @param registerInfoVO 注册信息
     * @return UserDO
     */
    @Override
    public Result<UserVO> bindPhone(RegisterInfoVO registerInfoVO) {
        // 检验验证码是否正确
        boolean b = checkSmsCode(
                RedisKey.build(RedisConstant.REDIS_KEY_SMS_BIND_PHONE_CODE,registerInfoVO.getPhoneNumber()),
                registerInfoVO.getSmsCode());
        if(!b){
            throw new BusinessException(ExceptionMsgEnum.SMS_CODE_ERROR);
        }
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserDO::getPhoneNumber, registerInfoVO.getPhoneNumber());
        UserDO userDO = this.getOne(queryWrapper);
        if (Objects.nonNull(userDO)){
            throw new BusinessException(ExceptionMsgEnum.PHONE_NUMBER_EXIST);
        }
        // 注册
        UserDO newUserDO=registerAccountByThird(registerInfoVO);
        return generateUserVO(newUserDO);
    }
    /**
     * 重置密码
     * @param phoneNumber 手机号
     * @param password 密码
     * @param smsCode 验证码
     * @return Result<?>
     */
    @Override
    public Result<?> resetPassword(String phoneNumber, String password, String smsCode) {
        boolean b = checkSmsCode(
                RedisKey.build(RedisConstant.REDIS_KEY_SMS_RESET_PASSWORD_PHONE_CODE,phoneNumber),
                smsCode);
        if(!b){
            throw new BusinessException(ExceptionMsgEnum.SMS_CODE_ERROR);
        }
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserDO::getPhoneNumber, phoneNumber);
        UserDO userDO = this.getOne(queryWrapper);
        if (Objects.isNull(userDO)){
            throw new BusinessException(ExceptionMsgEnum.PHONE_NUMBER_NOT_REGISTER);
        }
        // 利用MD5加密密码，并且通过手机号给密码加盐
        String md5 = Md5Util.getMd5(phoneNumber + password);
        userDO.setPassword(md5);
        try {
            this.updateById(userDO);
        } catch (Exception e) {
            throw new SystemException(ExceptionMsgEnum.DB_ERROR, e);
        }
        return ResultUtil.successPost("重置密码成功", null);
    }
    /**
     * 通过手机号注册
     * @param registerInfoVO 注册信息
     * @return UserDO
     */
    @Override
    public Result<UserVO> register(RegisterInfoVO registerInfoVO) {
        // 检验验证码是否正确
        boolean b = checkSmsCode(
                RedisKey.build(RedisConstant.REDIS_KEY_SMS_REGISTER_PHONE_CODE,registerInfoVO.getPhoneNumber()),
                registerInfoVO.getSmsCode());
        if(!b){
            throw new BusinessException(ExceptionMsgEnum.SMS_CODE_ERROR);
        }
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserDO::getPhoneNumber, registerInfoVO.getPhoneNumber());
        UserDO userDO = this.getOne(queryWrapper);
        if (Objects.nonNull(userDO)){
            throw new BusinessException(ExceptionMsgEnum.PHONE_NUMBER_EXIST);
        }
        // 注册
        UserDO newUserDO=new UserDO();
        newUserDO.setPhoneNumber(registerInfoVO.getPhoneNumber());
        newUserDO.setPassword(Md5Util.getMd5(registerInfoVO.getPhoneNumber() + registerInfoVO.getPassword()));
        newUserDO.setNickname(DEFAULT_NICKNAME_PREFIX + CodeUtil.createNickname());
        newUserDO.setSex(2);
        newUserDO.setAvatarUrl("https://pmall-yyh.oss-cn-chengdu.aliyuncs.com/00001.jpg");
        String uid = CodeUtil.createUid(registerInfoVO.getPhoneNumber());
        newUserDO.setUid(uid);
        newUserDO.setHomePageBackground("https://pmall-yyh.oss-cn-chengdu.aliyuncs.com/IMG_20231212_011126.jpg");
        newUserDO.setAccountStatus(0);
        try {
            this.save(newUserDO);
        } catch (Exception e) {
            throw new SystemException(ExceptionMsgEnum.DB_ERROR, e);
        }
        return ResultUtil.successPost("注册成功", null);
    }

    @Override
    public Result<UserVO> getUserInfo(Long userId) {
        if(Objects.isNull(userId)){
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        UserDO userDO = this.getById(userId);
        if(Objects.isNull(userDO)){
            throw new BusinessException(ExceptionMsgEnum.TOKEN_INVALID);
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userDO, userVO);
        return ResultUtil.successGet("获取用户信息成功", userVO);
    }

    /**
     * 检验验证码是否正确
     * @param key redis key
     * @param smsCode 验证码
     */
    private boolean checkSmsCode(String key, String smsCode) {
        String s = (String) redisCache.get(key);
        return !Objects.isNull(s) && s.equals(smsCode);
    }

    /**
     * 生成UserVO
     * @param userDO 用户信息
     * @return UserVO
     */
    private Result<UserVO> generateUserVO(UserDO userDO) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userDO, userVO);
        // 生成token
        Map<String,Object> claims = new HashMap<>();
        claims.put("userId", userDO.getId());
        String token = JWTUtil.createToken(claims);
        // 利用redis设置token过期时间，过期时间为登录时间+1天
        redisCache.set(
                RedisKey.build(
                        RedisConstant.REDIS_KEY_USER_LOGIN_EXPIRE,
                        String.valueOf(userDO.getId())),
                System.currentTimeMillis() + jwtProperties.getExpireTime()) ;
        return ResultUtil.successPost(token, userVO);
    }
    /**
     * 通过第三方账号注册
     * @param registerInfoVO 注册信息
     * @return UserDO
     */
    private UserDO registerAccountByThird(RegisterInfoVO registerInfoVO) {
        UserDO newUserDO = new UserDO();
        BeanUtils.copyProperties(registerInfoVO, newUserDO);
        if(registerInfoVO.getRegisterType()==1) {
            newUserDO.setWxOpenId(registerInfoVO.getOpenId());
        }else if(registerInfoVO.getRegisterType()==2){
            newUserDO.setQqOpenId(registerInfoVO.getOpenId());
        }else if(registerInfoVO.getRegisterType()==3){
            newUserDO.setFacebookOpenId(registerInfoVO.getOpenId());
        }
        String uid = CodeUtil.createUid(registerInfoVO.getPhoneNumber());
        newUserDO.setUid(uid);
        newUserDO.setPassword(null);
        newUserDO.setSex(2);
        newUserDO.setHomePageBackground("https://pmall-yyh.oss-cn-chengdu.aliyuncs.com/IMG_20231212_011126.jpg");
        newUserDO.setAccountStatus(0);
        if(!StringUtils.hasText(registerInfoVO.getNickname())) {
            newUserDO.setNickname(DEFAULT_NICKNAME_PREFIX + CodeUtil.createNickname());
        }
        if(!StringUtils.hasText(registerInfoVO.getAvatarUrl())) {
            newUserDO.setAvatarUrl("https://pmall-yyh.oss-cn-chengdu.aliyuncs.com/00001.jpg");
        }
        try {
            this.save(newUserDO);
        } catch (Exception e) {
            throw new SystemException(ExceptionMsgEnum.DB_ERROR, e);
        }
        return newUserDO;
    }

}




