package com.yyh.xfs.common.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * @author yyh
 * @date 2023-12-09
 */
public class AddressAnalyzeUtil {
    public static String getAddress(String ip){
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return "获取位置失败";
        }
        if ("127.0.0.1".equals(ip)||"localhost".equals(ip)) {
            return "本地测试";
        }
        String url = "https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php?resource_id=6006&format=json&query=" + ip;
        HttpResponse res = HttpRequest.get(url).execute();
        if (200 != res.getStatus()) {
            return "获取位置失败";
        } else {
            JSONObject jsonObject = JSON.parseObject(res.body());
            JSONArray data = JSONArray.parseArray(jsonObject.get("data").toString());
            Map<String,String> map=(Map<String, String>) data.get(0);
            return map.get("location");
        }
    }
}
