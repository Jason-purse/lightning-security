package com.generatera.resource.server.specification.token.jwt.bearer;

import com.generatera.resource.server.specification.token.jwt.config.JwtAuthenticationToken;
/**
 * @author FLJ
 * @date 2023/1/5
 * @time 13:53
 * @Description BearerToken 直接解析token中的东西,不需要 authorities ..
 */
public class BearerAuthenticationToken extends JwtAuthenticationToken {

    public BearerAuthenticationToken(String token) {
        super(token);
    }

}