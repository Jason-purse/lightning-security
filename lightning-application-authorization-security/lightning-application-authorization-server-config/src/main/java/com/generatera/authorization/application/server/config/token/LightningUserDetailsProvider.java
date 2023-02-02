package com.generatera.authorization.application.server.config.token;

import com.generatera.security.authorization.server.specification.LightningUserPrincipal;

/**
 * @author FLJ
 * @date 2023/1/31
 * @time 10:30
 * @Description 将代理到各种登录授权服务器的特定实现
 */
public interface LightningUserDetailsProvider {

    /**
     *
     * @param authRefreshTokenAuthenticationToken 是一个未认证的 token
     * @param principalName 唯一约束 ..
     */
    LightningUserPrincipal getUserDetails(AuthRefreshTokenAuthenticationToken authRefreshTokenAuthenticationToken, String principalName);
}
