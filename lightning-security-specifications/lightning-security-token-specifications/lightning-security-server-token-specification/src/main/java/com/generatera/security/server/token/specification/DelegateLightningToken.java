package com.generatera.security.server.token.specification;

import org.springframework.util.Assert;

import java.time.Instant;

public class DelegateLightningToken implements LightningToken.LightningRefreshToken {
    
    private final LightningToken delegate;
    
    public DelegateLightningToken(LightningToken delegate) {
        Assert.notNull(delegate," token cannot be empty !!!");
        this.delegate = delegate;
    }
   

    @Override
    public String getTokenValue() {
        return delegate.getTokenValue();
    }

    @Override
    public Instant getIssuedAt() {
        return delegate.getIssuedAt();
    }

    @Override
    public Instant getExpiresAt() {
        return delegate.getExpiresAt();
    }
}
