package com.yyh.xfs.notes.feign;

import com.yyh.xfs.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author yyh
 * @date 2024-01-26
 */
@FeignClient("xfs-user")
public interface UserFeign {

    @GetMapping("/user/getUserInfo")
    Result<?> getUserInfo(@RequestParam("userId") Long userId);
}
