package com.generatera.authorization.server.common.configuration.token;

import com.generatera.authorization.server.common.configuration.ext.oauth2.provider.ProviderContext;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.Authentication;

public interface LightningAuthenticationParseContext extends LightningAuthenticationSecurityContext {
    @Nullable
    String getTokenValue();


    LightningToken.TokenType getTokenType();

    Boolean isPlain();

    public static LightningAuthenticationParseContext of(
            String tokenValue,
            Boolean isPlain,
            LightningToken.TokenType tokenType,
            LightningAuthenticationSecurityContext securityContext
    ) {
        return new DefaultLightningAuthenticationParseContext(securityContext, tokenValue,isPlain,tokenType);
    }
}

class DefaultLightningAuthenticationParseContext implements LightningAuthenticationParseContext {

    private final LightningAuthenticationSecurityContext delegate;

    private final String tokenValue;

    private final Boolean isPlain;

    private final LightningToken.TokenType tokenType;

    public DefaultLightningAuthenticationParseContext(LightningAuthenticationSecurityContext delegate, String tokenValue,
                                                      Boolean isPlain, LightningToken.TokenType tokenType) {
        this.delegate = delegate;
        this.tokenValue = tokenValue;
        this.isPlain = isPlain;
        this.tokenType = tokenType;
    }

    @Override
    public String getTokenValue() {
        return tokenValue;
    }

    public Boolean isPlain() {
        return isPlain;
    }

    @Override
    public LightningToken.TokenType getTokenType() {
        return tokenType;
    }

    @Override
    public Authentication getAuthentication() {
        return delegate.getAuthentication();
    }

    @Override
    public ProviderContext getProviderContext() {
        return delegate.getProviderContext();
    }

    @Override
    public TokenSettings getTokenSettings() {
        return delegate.getTokenSettings();
    }
}
