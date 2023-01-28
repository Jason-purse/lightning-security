package com.generatera.authorization.application.server.config.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Objects;
/**
 * @author FLJ
 * @date 2023/1/28
 * @time 17:22
 * @Description 基于 Username/password authentication token
 */
public class AuthUsernamePasswordAuthenticationToken extends AbstractAuthenticationToken {

    private final UsernamePasswordAuthenticationToken authentication;


    public AuthUsernamePasswordAuthenticationToken(UsernamePasswordAuthenticationToken authentication) {
        super(Objects.requireNonNull(authentication).getAuthorities());
        this.setAuthenticated(true);
        this.authentication = authentication;
    }

    @Override
    public Object getCredentials() {
        return authentication.getCredentials();
    }

    @Override
    public Object getPrincipal() {
        return authentication.getPrincipal();
    }
}