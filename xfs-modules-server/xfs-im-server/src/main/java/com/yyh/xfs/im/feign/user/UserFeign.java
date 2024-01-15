package com.yyh.xfs.im.feign.user;

import com.yyh.xfs.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author yyh
 * @date 2024-01-15
 */
@FeignClient("xfs-user")
public interface UserFeign {
    /**
     * 查询用户是否被拉黑
     *
     * @param toId   被拉黑用户id
     * @param fromId 拉黑用户id
     * @param token  token，由于websocket无法获取header，所以需要在参数中传递
     * @return 是否被拉黑
     */
    @GetMapping(value = "/user/relation/isBlack")
    Result<Boolean> selectOneByUserIdAndBlackIdIsExist(@RequestParam("toId") Long toId, @RequestParam("fromId") Long fromId, @RequestHeader("token") String token);

    /**
     * 查询用户是否被关注
     *
     * @param toId   被关注用户id
     * @param fromId 关注用户id
     * @param token  token，由于websocket无法获取header，所以需要在参数中传递
     * @return 是否被关注
     */
    @GetMapping("/user/relation/isAttention")
    Result<Boolean> selectOneByUserIdAndAttentionIdIsExist(@RequestParam("toId") Long toId, @RequestParam("fromId") Long fromId, @RequestHeader("token") String token);
}
