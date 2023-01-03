package com.generatera.authorization.server.common.configuration.token;

import org.springframework.util.Assert;

/**
 * 代理实现 生成 LightningAuthentication Token
 */
public class DelegateLightningAuthenticationTokenGenerator implements LightningAuthenticationTokenGenerator {
    private final LightningAuthenticationTokenGenerator delegate;

    public DelegateLightningAuthenticationTokenGenerator(LightningAuthenticationTokenGenerator delegate) {
        Assert.notNull(delegate,"delegate LightningAuthenticationTokenGenerator must not be null !!!");
        this.delegate = delegate;
    }

    @Override
    public LightningAuthenticationToken generate(LightningAuthenticationSecurityContext securityContext) {
        return delegate.generate(securityContext);
    }
}
