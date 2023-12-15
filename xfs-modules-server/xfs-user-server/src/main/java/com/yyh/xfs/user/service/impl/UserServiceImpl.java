package com.yyh.xfs.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.redis.constant.RedisConstant;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.common.redis.utils.RedisKey;
import com.yyh.xfs.common.utils.CodeUtil;
import com.yyh.xfs.common.utils.Md5Util;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.common.web.properties.JwtProperties;
import com.yyh.xfs.common.web.utils.JWTUtil;
import com.yyh.xfs.user.domain.UserDO;
import com.yyh.xfs.user.service.UserService;
import com.yyh.xfs.user.mapper.UserMapper;
import com.yyh.xfs.user.vo.RegisterInfoVO;
import com.yyh.xfs.user.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
* @author 86131
* @description 针对表【user】的数据库操作Service实现
* @createDate 2023-12-11 15:26:32
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService{

    private final JwtProperties jwtProperties;
    private final RedisCache redisCache;

    public UserServiceImpl(RedisCache redisCache, JwtProperties jwtProperties) {
        this.redisCache = redisCache;
        this.jwtProperties = jwtProperties;
    }

//    private static final Map<Integer, String> LOGIN_TYPE_MAP =new HashMap<>();

//    static {
//        LOGIN_TYPE_MAP.put(1, "wx_open_id");
//        LOGIN_TYPE_MAP.put(2, "qq_open_id");
//        LOGIN_TYPE_MAP.put(3, "facebook_open_id");
//    }
    private static final Map<Integer, SFunction<UserDO, String>> LOGIN_TYPE_MAP = new HashMap<>();

    @PostConstruct
    public void postConstruct() {
        LOGIN_TYPE_MAP.put(1, UserDO::getWxOpenId);
        LOGIN_TYPE_MAP.put(2, UserDO::getQqOpenId);
        LOGIN_TYPE_MAP.put(3, UserDO::getFacebookOpenId);
    }
    @Override
    public Result<UserVO> login(String phoneNumber, String password) {
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
        // 利用MD5加密密码，并且通过手机号给密码加盐
        String md5Password = Md5Util.getMd5(phoneNumber + password);
        queryWrapper.lambda().eq(UserDO::getPhoneNumber, phoneNumber).eq(UserDO::getPassword, md5Password);
        UserDO userDO = this.getOne(queryWrapper);
        if (Objects.isNull(userDO)) {
            return ResultUtil.errorPost("用户名或密码错误");
        }
        return generateUserVO(userDO);
    }

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

    @Override
    public Result<UserVO> bindPhone(RegisterInfoVO registerInfoVO) {
        // 检验验证码是否正确
        boolean b = checkSmsCode(
                RedisKey.build(RedisConstant.REDIS_KEY_SMS_BIND_PHONE_CODE,registerInfoVO.getPhoneNumber()),
                registerInfoVO.getSmsCode());
        if(!b){
            return ResultUtil.errorPost("验证码错误");
        }
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserDO::getPhoneNumber, registerInfoVO.getPhoneNumber());
        UserDO userDO = this.getOne(queryWrapper);
        if (Objects.nonNull(userDO)){
            return ResultUtil.errorPost("该手机号已经注册过");
        }
        // 注册
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
        newUserDO.setHomePageBackground("https://pmall-yyh.oss-cn-chengdu.aliyuncs.com/IMG_20231212_011126.jpg");
        newUserDO.setAccountStatus(0);
        try {
            this.save(newUserDO);
        } catch (Exception e) {
            log.error("数据库插入失败", e);
            throw new RuntimeException(e);
        }
        return generateUserVO(newUserDO);
    }

    @Override
    public Result<?> resetPassword(String phoneNumber, String password, String smsCode) {
        boolean b = checkSmsCode(
                RedisKey.build(RedisConstant.REDIS_KEY_SMS_RESET_PASSWORD_PHONE_CODE,phoneNumber),
                smsCode);
        if(!b){
            return ResultUtil.errorPost("验证码错误");
        }
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserDO::getPhoneNumber, phoneNumber);
        UserDO userDO = this.getOne(queryWrapper);
        if (Objects.isNull(userDO)){
            return ResultUtil.errorPost("该手机号未注册");
        }
        // 利用MD5加密密码，并且通过手机号给密码加盐
        String md5 = Md5Util.getMd5(phoneNumber + password);
        userDO.setPassword(md5);
        try {
            this.updateById(userDO);
        } catch (Exception e) {
            log.error("数据库更新失败", e);
            throw new RuntimeException(e);
        }
        return ResultUtil.successPost("重置密码成功", null);
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
}




