package com.yyh.xfs.im.feign.user;

import com.yyh.xfs.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author yyh
 * @date 2024-01-15
 */
@FeignClient("xfs-user")
public interface UserFeign {
    @GetMapping("/user/relation/isBlack")
    Result<Boolean> selectOneByUserIdAndBlackIdIsExist(@RequestParam("toId") Long toId, @RequestParam("fromId") Long fromId);

    @GetMapping("/user/relation/isAttention")
    Result<Boolean> selectOneByUserIdAndAttentionIdIsExist(@RequestParam("toId") Long toId, @RequestParam("fromId") Long fromId);
}
