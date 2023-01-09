package com.generatera.security.authorization.server.specification.components.token;

public class DelegateLightningTokenGenerator implements LightningTokenGenerator<LightningToken> {
    private final LightningTokenGenerator<LightningToken> delegate;
    public DelegateLightningTokenGenerator(LightningTokenGenerator<LightningToken> tokenGenerator) {
        this.delegate = tokenGenerator;
    }
    @Override
    public LightningToken generate(LightningSecurityTokenContext context) {
        return delegate.generate(context);
    }
}
