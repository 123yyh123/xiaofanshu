package com.yyh.xfs.common.utils;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author yyh
 * @date 2023-12-09
 */
public class Md5Util {
    public static String getMd5(String str) {
        return DigestUtils.md5Hex(str);
    }
}
