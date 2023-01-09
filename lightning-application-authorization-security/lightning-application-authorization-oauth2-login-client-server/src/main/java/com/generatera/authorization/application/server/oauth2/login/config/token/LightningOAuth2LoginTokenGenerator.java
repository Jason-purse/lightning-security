package com.generatera.authorization.application.server.oauth2.login.config.token;

import com.generatera.security.authorization.server.specification.components.token.LightningToken;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenGenerator;

/**
 * 针对oauth2 的 authentication token的生成 ...
 */
public interface LightningOAuth2LoginTokenGenerator extends LightningTokenGenerator<LightningToken> {

}
