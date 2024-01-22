package com.yyh.xfs.notes.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author yyh
 * @date 2024-01-22
 */
@Configuration
@Getter
public class BaiduAiConfig {
    @Value("${baiduyun.ai.apiKey}")
    private String apiKey;

    @Value("${baiduyun.ai.secretKey}")
    private String secretKey;

}
