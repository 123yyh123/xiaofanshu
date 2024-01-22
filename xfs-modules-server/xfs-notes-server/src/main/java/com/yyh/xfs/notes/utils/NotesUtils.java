package com.yyh.xfs.notes.utils;

import com.alibaba.fastjson.JSON;
import com.yyh.xfs.notes.config.BaiduAiConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.*;

/**
 * @author yyh
 * @date 2024-01-22
 */
@Configuration
@Slf4j
public class NotesUtils {
    private static BaiduAiConfig baiduAiConfig;

    @Autowired
    private void setJwtProperties(BaiduAiConfig baiduAiConfig) {
        NotesUtils.baiduAiConfig = baiduAiConfig;
    }

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

    /**
     * 利用百度AI接口进行文章分类
     * @param title 文章标题
     * @param content 文章内容
     * @return 分类结果
     */
    public static String createCategory(String title, String content) {
        try {
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, buildJsonRequestBody(title, content));
            Request request = new Request.Builder()
                    .url("https://aip.baidubce.com/rpc/2.0/nlp/v1/topic?charset=UTF-8&access_token=" + getAccessToken())
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build();

            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                if (response.body() != null) {
                    String s= JSON.parseObject(response.body().string()).getJSONObject("item").getString("lv1_tag_list");
                    return JSON.parseArray(s).getJSONObject(0).getString("tag");
                }
            }
        } catch (IOException | JSONException e) {
            // 处理异常，例如记录日志
            log.error("createCategory error", e);
        }
        return null;
    }

    private static String getAccessToken() throws IOException, JSONException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + baiduAiConfig.getApiKey()
                + "&client_secret=" + baiduAiConfig.getSecretKey());
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (response.body() != null) {
                return new JSONObject(response.body().string()).getString("access_token");
            }
        }
        return null;
    }

    private static String buildJsonRequestBody(String title, String content) throws JSONException {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("title", title);
        jsonBody.put("content", content);
        return jsonBody.toString();
    }
}

