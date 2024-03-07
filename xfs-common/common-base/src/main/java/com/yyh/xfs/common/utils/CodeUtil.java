package com.yyh.xfs.common.utils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author yyh
 * @date 2023-12-12
 */

public class CodeUtil {

    /**
     * 生成6位随机数，用于短信验证码
     *
     * @return 6位随机数
     */
    public static String createSmsCode() {
        // 使用线程安全的Random
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int code = random.nextInt(100000, 1000000);
        return String.valueOf(code);
    }

    /**
     * 生成用户uid，6-15位组成，首位不能为0，由数字,英文字母和下划线组成，没有强制要求，三种组合都可以
     * 暂时用手机号码后四位和当前时间戳的3-6位相加，再加3-6为替换掉当前时间戳的3-6位，只能有数字组成
     *
     * @param phoneNumber 手机号码
     * @return 用户uid
     */
    public static String createUid(String phoneNumber) {
        long currentTimeMillis = System.currentTimeMillis();
        String currentTimeMillisStr = String.valueOf(currentTimeMillis);
        int currentTimeMillisStrLength = currentTimeMillisStr.length();
        //利用手机号码后四位与当前时间戳的3-6位相加，再加3-6为替换掉当前时间戳的3-6位
        String substring = currentTimeMillisStr.substring(2, 6);
        int i = Integer.parseInt(substring);
        int i1 = Integer.parseInt(phoneNumber.substring(7));
        int i2 = i + i1;
        String i2Str = String.valueOf(i2);
        String substring1 = currentTimeMillisStr.substring(0, 2);
        String substring2 = currentTimeMillisStr.substring(6);
        return substring1 + i2Str + substring2;
    }

    /**
     * 生成用户昵称，8位随机数，由数字,英文字母组成，用户昵称是可以重复的，不需要唯一
     */
    public static String createNickname() {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 根据hash生成下标，size必须是2的幂
     *
     * @param object 对象
     * @param size   长度 必须是2的幂
     * @return 下标
     */
    public static int hashIndex(Object object, int size) {
        // 判断size是否是2的幂
        if ((size & (size - 1)) != 0) {
            throw new IllegalArgumentException("size must be a power of 2");
        }
        int h;
        int hash = (h = object.hashCode()) ^ (h >>> size);
        return hash & (size - 1);
    }

    /**
     * 根据hash生成下标，size必须是2的幂
     *
     * @param object 对象
     * @return 下标
     */
    public static int hashIndex(Object object) {
        return hashIndex(object, 16);
    }
}
