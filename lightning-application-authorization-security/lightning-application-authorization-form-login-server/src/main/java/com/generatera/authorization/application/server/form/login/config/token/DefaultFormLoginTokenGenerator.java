package com.generatera.authorization.application.server.form.login.config.token;

import com.generatera.security.authorization.server.specification.components.token.LightningSecurityTokenContext;
import com.generatera.security.authorization.server.specification.components.token.LightningToken;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenGenerator;

public class DefaultFormLoginTokenGenerator implements FormLoginTokenGenerator {
    private final LightningTokenGenerator<LightningToken> tokenGenerator;
    public DefaultFormLoginTokenGenerator(LightningTokenGenerator<LightningToken> tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
    }
    @Override
    public LightningToken generate(LightningSecurityTokenContext context) {
        return tokenGenerator.generate(context);
    }
}
