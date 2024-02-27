package com.yyh.xfs.common.web.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author yyh
 * @date 2023-09-17
 */
public class IPUtils {

    /**
     * 获取真实ip地址
     *
     * @param request 请求
     * @return ip地址
     */
    public static String getRealIpAddr(HttpServletRequest request) {
        String ip = null;
        try {
            ip = request.getHeader("x-forwarded-for");
        } catch (Exception e) {
            return null;
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (null != ip && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }

    /**
     * 根据ip获取地址
     *
     * @param ip ip地址
     * @return 地址
     */
    public static String getAddrByIp(String ip) {
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return null;
        }
        if ("127.0.0.1".equals(ip)) {
            return null;
        }
        String url = "https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php?resource_id=6006&format=json&query=" + ip;
        HttpResponse res = HttpRequest.get(url).execute();
        if (0 != res.getStatus()) {
            return null;
        } else {
            JSONObject jsonObject = JSON.parseObject(res.body());
            JSONArray data = JSONArray.parseArray(jsonObject.get("data").toString());
            Map<String, String> map = (Map<String, String>) data.get(0);
            return map.get("location");
        }
    }

    /**
     * 切割地址，供前端使用
     *
     * @param address 地址
     */
    public static String splitAddress(String address) {
        if (!StringUtils.hasText(address)) {
            return "";
        }
        String province = "";
        String city;
        if (address.contains("市")) {
            if (address.contains("省")) {
                province = address.substring(0, address.indexOf("省"));
                city = address.substring(address.indexOf("省") + 1, address.indexOf("市"));
            } else {
                city = address.substring(0, address.indexOf("市"));
            }
            return province + city;
        } else {
            return address;
        }
    }

    /**
     * 获取省份，若为直辖市则返回直辖市，若为外国则返回国家
     *
     * @param address 地址
     * @return 省份
     */
    public static String provinceAddress(String address) {
        if (!StringUtils.hasText(address)) {
            return "";
        }
        if (address.contains("省")) {
            return address.substring(0, address.indexOf("省"));
        } else if (address.contains("市")) {
            return address.substring(0, address.indexOf("市"));
        } else {
            return address;
        }
    }
}
