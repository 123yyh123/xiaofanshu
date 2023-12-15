package com.yyh.xfs.gateway.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author yyh
 * @date 2023-12-11
 * @desc 不需要token的路径
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "release.auth")
public class ReleasePath {
    private List<String> path;
}
