package com.generatera.authorization.application.server.form.login.config.token;

import com.generatera.authorization.server.common.configuration.token.LightningSecurityContext;
import com.generatera.authorization.server.common.configuration.token.LightningToken;
import com.generatera.authorization.server.common.configuration.token.ProviderContext;
import com.generatera.authorization.server.common.configuration.token.TokenSettings;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;

public interface FormLoginSecurityContext  extends LightningSecurityContext {




    static FormLoginSecurityContext of(LightningToken.TokenType tokenType,
                                       Authentication authentication,
                                       ProviderContext providerContext,
                                       TokenSettings tokenSettings) {

        return new DefaultFormLoginSecurityContext(tokenType,
                authentication,
                providerContext,
                tokenSettings
        );
    }
}

@AllArgsConstructor
class DefaultFormLoginSecurityContext implements FormLoginSecurityContext {


    private LightningToken.TokenType tokenType;

    private Authentication authentication;

    private ProviderContext providerContext;

    private TokenSettings tokenSettings;

    @Override
    public LightningToken.TokenType getTokenType() {
        return tokenType;
    }

    @Override
    public Authentication getAuthentication() {
        return authentication;
    }

    @Override
    public ProviderContext getProviderContext() {
        return providerContext;
    }

    @Override
    public TokenSettings getTokenSettings() {
        return tokenSettings;
    }
}
