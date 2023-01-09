package com.generatera.security.authorization.server.specification.components.token;

import com.generatera.security.authorization.server.specification.components.token.LightningToken;
import org.springframework.util.Assert;

import java.time.Instant;

public class DelegateLightningToken implements LightningToken {
    
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
