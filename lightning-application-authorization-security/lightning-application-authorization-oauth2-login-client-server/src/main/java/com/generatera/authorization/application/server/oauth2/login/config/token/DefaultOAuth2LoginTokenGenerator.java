package com.generatera.authorization.application.server.oauth2.login.config.token;

import com.generatera.security.authorization.server.specification.components.token.LightningSecurityTokenContext;
import com.generatera.security.authorization.server.specification.components.token.LightningToken;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenGenerator;
import org.springframework.util.Assert;

public class DefaultOAuth2LoginTokenGenerator implements LightningOAuth2LoginTokenGenerator {

    private final LightningTokenGenerator<LightningToken> delegate;
    public DefaultOAuth2LoginTokenGenerator(LightningTokenGenerator<LightningToken> tokenGenerator) {
        Assert.notNull(tokenGenerator,"tokenGenerator must not be null !!!");
        this.delegate =  tokenGenerator;
    }

    @Override
    public LightningToken generate(LightningSecurityTokenContext context) {
        return delegate.generate(context);
    }
}
