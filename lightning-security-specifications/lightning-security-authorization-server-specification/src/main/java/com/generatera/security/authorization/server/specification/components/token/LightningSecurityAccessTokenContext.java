package com.generatera.security.authorization.server.specification.components.token;

import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.TokenIssueFormat;
import com.generatera.security.authorization.server.specification.TokenSettings;
import com.generatera.security.authorization.server.specification.components.provider.ProviderContext;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningTokenValueType;
import org.springframework.security.core.Authentication;

/**
 *  Security 访问Token Context
 */
public class LightningSecurityAccessTokenContext implements LightningSecurityTokenContext {

    private final LightningSecurityTokenContext securityTokenContext;

    public LightningSecurityAccessTokenContext(LightningSecurityTokenContext securityTokenContext) {
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
    public LightningTokenValueType getTokenValueType() {
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
