package com.generatera.authorization.server.oauth2.configuration.token;

import com.generatera.authorization.application.server.config.token.LightningOAuth2ServerTokenGenerator;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;

public class DefaultLightningOAuth2ServerTokenGenerator implements LightningOAuth2ServerTokenGenerator {

    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
    public DefaultLightningOAuth2ServerTokenGenerator(OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
        Assert.notNull(tokenGenerator,"tokenGenerator must not be null !!!");
        this.tokenGenerator = tokenGenerator;
    }


    @Override
    public OAuth2Token generate(OAuth2TokenContext context) {
        return tokenGenerator.generate(context);
    }
}
