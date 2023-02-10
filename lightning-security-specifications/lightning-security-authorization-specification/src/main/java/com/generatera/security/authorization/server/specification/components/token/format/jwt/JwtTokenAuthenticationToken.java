package com.generatera.security.authorization.server.specification.components.token.format.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.util.Assert;

import java.util.Collections;
/**
 * @author FLJ
 * @date 2023/1/6
 * @time 15:33
 * @Description Jwt Token(Authentication)
 *
 * jwt token中的 claims 已经包含了 {@code authorities}
 */
public class JwtTokenAuthenticationToken extends AbstractAuthenticationToken {

    public JwtTokenAuthenticationToken(String token) {
        super(Collections.emptyList());
        Assert.hasText(token, "token cannot be empty");
        this.token = token;
    }

    private final String token;


    public String getToken() {
        return this.token;
    }

    public Object getCredentials() {
        return this.getToken();
    }

    public Object getPrincipal() {
        return this.getToken();
    }
}
