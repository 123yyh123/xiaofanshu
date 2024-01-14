package com.yyh.xfs.common.web.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author yyh
 * @date 2024-01-13
 */
@Configuration
@ConfigurationProperties(prefix = "tencentyun.trtc")
@Getter
@Setter
public class TrtcProperties {
    private long sdkAppId;
    private String secretKey;
    private long expireTime;
}
