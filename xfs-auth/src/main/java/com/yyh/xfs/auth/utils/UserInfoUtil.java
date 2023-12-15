package com.yyh.xfs.auth.utils;
import com.yyh.xfs.common.web.properties.JwtProperties;
import com.yyh.xfs.common.web.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author yyh
 * @date 2023-10-01
 */
@Configuration
public class UserInfoUtil {
    public static Long getUserId(HttpServletRequest request) {
        String token = null;
        try {
            token = request.getHeader("token");
        } catch (Exception e) {
            return null;
        }
        if(token == null){
            return null;
        }
        Long userId=null;
        try {
            Map<String, Object> map = JWTUtil.parseToken(token);
            userId = (Long) map.get("userId");

        } catch (Exception e) {
            return null;
        }
        return userId;
    }
}
