package com.yyh.xfs.auth.utils;
import com.tencentyun.TLSSigAPIv2;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.web.exception.BusinessException;
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
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN, e);
        }
        if(token == null){
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        Long userId=null;
        try {
            Map<String, Object> map = JWTUtil.parseToken(token);
            userId = (Long) map.get("userId");
        } catch (Exception e) {
            throw new BusinessException(ExceptionMsgEnum.TOKEN_INVALID, e);
        }
        return userId;
    }
}
