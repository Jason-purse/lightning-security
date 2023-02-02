package com.generatera.authorization.application.server.config.token;

import org.springframework.security.core.Authentication;

/**
 * 主要用来 校验用户信息,并返回认证过后的信息
 */
public interface LightningDaoAuthenticationProvider {

   Authentication authenticate(AuthAccessTokenAuthenticationToken authentication);
}
