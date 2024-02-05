package com.yyh.xfs.common.web.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author yyh
 * @date 2024-02-03
 */
@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "gaode.map")
public class GaoDeMapProperties {
    private String key;
}
