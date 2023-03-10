package com.generatera.authorization.application.server.config.token;

import org.springframework.security.core.Authentication;

/**
 * 主要用来 校验用户信息,并返回认证过后的信息
 *
 * 支持表单登录 / oauth2 等相关直接使用用户名和密码进行登录的方式 ...
 * 主要被{@link com.generatera.authorization.application.server.config.authentication.LightningAppAuthServerDaoLoginAuthenticationProvider}
 * 使用来进行校验之后返回验证的Authentication 的动作
 * @see com.generatera.authorization.application.server.config.authentication.LightningAppAuthServerDaoLoginAuthenticationProvider
 * @see com.generatera.authorization.application.server.config.util.AppAuthConfigurerUtils#getDaoAuthenticationProvider
 */
public interface LightningDaoAuthenticationProvider {

   Authentication authenticate(AuthAccessTokenAuthenticationToken authentication);
}
