package com.generatera.security.authorization.server.specification.components.token;

import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.TokenIssueFormat;
import com.generatera.security.authorization.server.specification.TokenSettings;
import com.generatera.security.authorization.server.specification.components.provider.ProviderContext;
import org.springframework.security.core.Authentication;

public class LightningSecurityRefreshTokenContext implements LightningSecurityTokenContext {
    private final LightningSecurityTokenContext securityTokenContext;
    public LightningSecurityRefreshTokenContext(LightningSecurityTokenContext securityTokenContext) {
        this.securityTokenContext = securityTokenContext;
    }
    @Override
    public Authentication getAuthentication() {
        return securityTokenContext.getAuthentication();
    }

    @Override
    public ProviderContext getProviderContext() {
        return securityTokenContext.getProviderContext();
    }

    @Override
    public TokenSettings getTokenSettings() {
        return securityTokenContext.getTokenSettings();
    }

    @Override
    public TokenIssueFormat getTokenFormat() {
        return securityTokenContext.getTokenFormat();
    }

    @Override
    public LightningTokenType.LightningTokenValueType getTokenValueType() {
        return securityTokenContext.getTokenValueType();
    }

    @Override
    public LightningTokenType.LightningAuthenticationTokenType getTokenType() {
        return securityTokenContext.getTokenType();
    }

    @Override
    public LightningUserPrincipal getPrincipal() {
        return securityTokenContext.getPrincipal();
    }
}
