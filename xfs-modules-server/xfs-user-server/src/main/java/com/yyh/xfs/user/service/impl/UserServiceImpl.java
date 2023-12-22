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
import com.yyh.xfs.common.utils.TimeUtil;
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
import java.sql.Date;
import java.text.SimpleDateFormat;
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
        if(Objects.nonNull(userDO.getBirthday())) {
            userVO.setBirthday(userDO.getBirthday().toString());
        }
        return ResultUtil.successGet("获取用户信息成功", userVO);
    }

    /**
     * 修改用户头像
     * @param userVO 用户信息
     * @return Result<?>
     */
    @Override
    public Result<?> updateAvatarUrl(UserVO userVO) {
        if(!StringUtils.hasText(String.valueOf(userVO.getId()))){
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        UserDO userDO = this.getById(userVO.getId());
        if(Objects.isNull(userDO)){
            throw new BusinessException(ExceptionMsgEnum.ACCOUNT_EXCEPTION);
        }
        if(!StringUtils.hasText(userVO.getAvatarUrl())){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        userDO.setAvatarUrl(userVO.getAvatarUrl());
        try {
            this.updateById(userDO);
            return ResultUtil.successPost("修改头像成功", null);
        } catch (Exception e) {
            throw new SystemException(ExceptionMsgEnum.DB_ERROR, e);
        }
    }
    /**
     * 修改用户背景图片
     * @param userVO 用户信息
     * @return Result<?>
     */
    @Override
    public Result<?> updateBackgroundImage(UserVO userVO) {
        if(!StringUtils.hasText(String.valueOf(userVO.getId()))){
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        UserDO userDO = this.getById(userVO.getId());
        if(Objects.isNull(userDO)){
            throw new BusinessException(ExceptionMsgEnum.ACCOUNT_EXCEPTION);
        }
        if(!StringUtils.hasText(userVO.getHomePageBackground())){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        userDO.setHomePageBackground(userVO.getHomePageBackground());
        try {
            this.updateById(userDO);
            return ResultUtil.successPost("修改背景成功", null);
        } catch (Exception e) {
            throw new SystemException(ExceptionMsgEnum.DB_ERROR, e);
        }
    }
    /**
     * 修改用户昵称
     * @param userVO 用户信息
     * @return Result<?>
     */
    @Override
    public Result<?> updateNickname(UserVO userVO) {
        if(!StringUtils.hasText(String.valueOf(userVO.getId()))){
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        UserDO userDO = this.getById(userVO.getId());
        if(Objects.isNull(userDO)){
            throw new BusinessException(ExceptionMsgEnum.ACCOUNT_EXCEPTION);
        }
        if (!StringUtils.hasText(userVO.getNickname())) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if(userVO.getNickname().length()>12||userVO.getNickname().length()<2){
            return ResultUtil.errorPost("昵称长度为2-12位");
        }
        userDO.setNickname(userVO.getNickname());
        try {
            this.updateById(userDO);
            return ResultUtil.successPost("修改昵称成功", null);
        } catch (Exception e) {
            throw new SystemException(ExceptionMsgEnum.DB_ERROR, e);
        }
    }
    /**
     * 修改用户简介
     * @param userVO 用户信息
     * @return Result<?>
     */
    @Override
    public Result<?> updateIntroduction(UserVO userVO) {
        if(!StringUtils.hasText(String.valueOf(userVO.getId()))){
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        UserDO userDO = this.getById(userVO.getId());
        if(Objects.isNull(userDO)){
            throw new BusinessException(ExceptionMsgEnum.ACCOUNT_EXCEPTION);
        }
        if (!StringUtils.hasText(userVO.getSelfIntroduction())) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if(userVO.getSelfIntroduction().length()>100){
            return ResultUtil.errorPost("简介长度不能超过100");
        }
        userDO.setSelfIntroduction(userVO.getSelfIntroduction());
        try {
            this.updateById(userDO);
            return ResultUtil.successPost("修改简介成功", null);
        } catch (Exception e) {
            throw new SystemException(ExceptionMsgEnum.DB_ERROR, e);
        }
    }
    /**
     * 修改用户性别
     * @param userVO 用户信息
     * @return Result<?>
     */
    @Override
    public Result<?> updateSex(UserVO userVO) {
        if(!StringUtils.hasText(String.valueOf(userVO.getId()))){
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        UserDO userDO = this.getById(userVO.getId());
        if(Objects.isNull(userDO)){
            throw new BusinessException(ExceptionMsgEnum.ACCOUNT_EXCEPTION);
        }
        if(Objects.isNull(userVO.getSex())||userVO.getSex()<0||userVO.getSex()>1){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        userDO.setSex(userVO.getSex());
        try {
            this.updateById(userDO);
            return ResultUtil.successPost("修改性别成功", null);
        } catch (Exception e) {
            throw new SystemException(ExceptionMsgEnum.DB_ERROR, e);
        }
    }
    /**
     * 修改用户生日
     * @param userVO 用户信息
     * @return Result<?>
     */
    @Override
    public Result<?> updateBirthday(UserVO userVO) {
        if(!StringUtils.hasText(String.valueOf(userVO.getId()))){
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        UserDO userDO = this.getById(userVO.getId());
        if(Objects.isNull(userDO)){
            throw new BusinessException(ExceptionMsgEnum.ACCOUNT_EXCEPTION);
        }
        if(Objects.isNull(userVO.getBirthday())){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        try {
            Date date = Date.valueOf(userVO.getBirthday());
            // 判断生日是否合法，不能大于当前时间
            long currentTimeMillis = System.currentTimeMillis();
            if (date.getTime() > currentTimeMillis) {
                throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
            }
            userDO.setBirthday(date);
            // 更新年龄
            int age = TimeUtil.calculateAge(date.toLocalDate());
            userDO.setAge(age);
        } catch (Exception e) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        try {
            this.updateById(userDO);
            return ResultUtil.successPost("修改生日成功", userDO.getAge());
        } catch (Exception e) {
            throw new SystemException(ExceptionMsgEnum.DB_ERROR, e);
        }
    }
    /**
     * 修改用户地区
     * @param userVO 用户信息
     * @return Result<?>
     */
    @Override
    public Result<?> updateArea(UserVO userVO) {
        if(!StringUtils.hasText(String.valueOf(userVO.getId()))){
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        UserDO userDO = this.getById(userVO.getId());
        if(Objects.isNull(userDO)){
            throw new BusinessException(ExceptionMsgEnum.ACCOUNT_EXCEPTION);
        }
        if(!StringUtils.hasText(userVO.getArea())){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        // 判断地区是否合法，如果不合法则抛出异常，格式为：省 市 区
        String[] split = userVO.getArea().split(" ");
        if(split.length!=3){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if(split[0].equals(split[1])){
            userDO.setArea(split[0]+" "+split[2]);
        }else {
            userDO.setArea(userVO.getArea());
        }
        try {
            this.updateById(userDO);
            return ResultUtil.successPost("修改地区成功", null);
        } catch (Exception e) {
            throw new SystemException(ExceptionMsgEnum.DB_ERROR, e);
        }
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
        if(Objects.nonNull(userDO.getBirthday())) {
            userVO.setBirthday(userDO.getBirthday().toString());
        }
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




