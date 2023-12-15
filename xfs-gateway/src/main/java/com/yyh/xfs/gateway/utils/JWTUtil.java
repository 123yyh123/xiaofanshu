package com.yyh.xfs.gateway.utils;

import com.yyh.xfs.gateway.properties.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author yyh
 * @date 2023-12-09
 */
@Configuration
public class JWTUtil {

    private static JwtProperties jwtProperties;

    @Autowired
    private void setJwtProperties(JwtProperties jwtProperties) {
        JWTUtil.jwtProperties = jwtProperties;
    }
    public static String createToken(Map<String,Object> claims){
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512,jwtProperties.getSecret()).compact();
    }

    public static Map<String,Object> parseToken(String token){
        return Jwts.parser().setSigningKey(jwtProperties.getSecret()).parseClaimsJws(token).getBody();
    }
}
