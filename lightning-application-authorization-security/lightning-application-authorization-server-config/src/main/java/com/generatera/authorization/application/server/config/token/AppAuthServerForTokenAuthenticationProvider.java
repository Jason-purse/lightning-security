package com.generatera.authorization.application.server.config.token;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * 认证提供器
 * 生成token,它将 产生 token
 */
public interface AppAuthServerForTokenAuthenticationProvider extends AuthenticationProvider {
    @Override
    AuthAccessTokenAuthenticationToken authenticate(Authentication authentication) throws AuthenticationException;
}
