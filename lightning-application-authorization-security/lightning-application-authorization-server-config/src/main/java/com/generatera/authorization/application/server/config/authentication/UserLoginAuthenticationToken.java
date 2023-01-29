package com.generatera.authorization.application.server.config.authentication;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.util.Assert;

import java.util.Collections;

/**
 * @author FLJ
 * @date 2023/1/29
 * @time 9:33
 * @Description  user login 认证token
 */
public class UserLoginAuthenticationToken extends AbstractAuthenticationToken {

    private final UsernamePasswordAuthenticationToken principal;


    public UserLoginAuthenticationToken(
            UsernamePasswordAuthenticationToken principal
    ) {
        super(Collections.emptyList());
        Assert.notNull(principal,"principal must not be null !!!");
        this.principal = principal;
    }

    @Override
    public Object getCredentials() {
        return principal.getCredentials();
    }

    @Override
    public Object getPrincipal() {
        return principal.getPrincipal();
    }

    public UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken() {
        return principal;
    }
}
