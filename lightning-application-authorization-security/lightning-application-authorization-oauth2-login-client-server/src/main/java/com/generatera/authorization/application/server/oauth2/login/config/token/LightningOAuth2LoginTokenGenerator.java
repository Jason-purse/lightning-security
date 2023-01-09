package com.generatera.authorization.application.server.oauth2.login.config.token;

import com.generatera.security.authorization.server.specification.components.token.LightningToken;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenGenerator;

/**
 * 主要用于 OAuth2 授权流程完成之后的LightningToken的生成工作扩展,
 * 如果不添加,默认使用 {@link com.generatera.security.authorization.server.specification.components.token.DelegatingLightningTokenGenerator}
 * 进行Token生成代理工作 ...
 *
 * 或者可以使用 {@link LightningTokenGenerator} 完成所有 LightningToken的生成工作 ...
 * @see com.generatera.security.authorization.server.specification.components.token.DefaultLightningAccessTokenGenerator
 * @see com.generatera.security.authorization.server.specification.components.token.DefaultLightningRefreshTokenGenerator
 * @see com.generatera.security.authorization.server.specification.components.token.format.jwt.LightningJwtGenerator
 */
public interface LightningOAuth2LoginTokenGenerator extends LightningTokenGenerator<LightningToken> {

}
