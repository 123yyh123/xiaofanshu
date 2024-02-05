package com.yyh.xfs.common.web.utils;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.yyh.xfs.common.web.properties.GaoDeMapProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yyh
 * @date 2024-02-03
 */
@Configuration
@Slf4j
public class AddressUtil {
    public static Map<String,String> provinceMap = new HashMap<>();
    public static GaoDeMapProperties gaoDeMapProperties;

    @Autowired
    private void setGaoDeMapProperties(GaoDeMapProperties gaoDeMapProperties) {
        AddressUtil.gaoDeMapProperties = gaoDeMapProperties;
    }

    static {
        provinceMap.put("北京市", "北京");
        provinceMap.put("天津市", "天津");
        provinceMap.put("河北省", "河北");
        provinceMap.put("山西省", "山西");
        provinceMap.put("内蒙古自治区", "内蒙古");
        provinceMap.put("辽宁省", "辽宁");
        provinceMap.put("吉林省", "吉林");
        provinceMap.put("黑龙江省", "黑龙江");
        provinceMap.put("上海市", "上海");
        provinceMap.put("江苏省", "江苏");
        provinceMap.put("浙江省", "浙江");
        provinceMap.put("安徽省", "安徽");
        provinceMap.put("福建省", "福建");
        provinceMap.put("江西省", "江西");
        provinceMap.put("山东省", "山东");
        provinceMap.put("河南省", "河南");
        provinceMap.put("湖北省", "湖北");
        provinceMap.put("湖南省", "湖南");
        provinceMap.put("广东省", "广东");
        provinceMap.put("广西壮族自治区", "广西");
        provinceMap.put("海南省", "海南");
        provinceMap.put("重庆市", "重庆");
        provinceMap.put("四川省", "四川");
        provinceMap.put("贵州省", "贵州");
        provinceMap.put("云南省", "云南");
        provinceMap.put("西藏自治区", "西藏");
        provinceMap.put("陕西省", "陕西");
        provinceMap.put("甘肃省", "甘肃");
        provinceMap.put("青海省", "青海");
        provinceMap.put("宁夏回族自治区", "宁夏");
        provinceMap.put("新疆维吾尔自治区", "新疆");
        provinceMap.put("台湾省", "台湾");
        provinceMap.put("香港特别行政区", "香港");
        provinceMap.put("澳门特别行政区", "澳门");
    }
    /**
     * 根据经纬度获取省份或国家
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 省份或国家
     */
    public static String getAddress(Double longitude, Double latitude) {
        String parameters = "?key=" + gaoDeMapProperties.getKey();
        parameters += "&location=" + longitude + "," + latitude;
        parameters += "&extensions=all";
        parameters += "&output=JSON";
        parameters += "&radius=100";
        String urlString = "https://restapi.amap.com/v3/geocode/regeo" + parameters;
        HttpResponse response = HttpUtil.createGet(urlString).execute();
        String body = response.body();
        // 解析json
        Map map = JSON.parseObject(body, Map.class);
        if ("1".equals(map.get("status"))) {
            Map regeocode = (Map) map.get("regeocode");
            Map addressComponent = (Map) regeocode.get("addressComponent");
            if ("中国".equals(addressComponent.get("country"))) {
                return provinceMap.get(addressComponent.get("province").toString());
            } else {
                return addressComponent.get("country").toString();
            }
        } else {
            return null;
        }
    }
}
