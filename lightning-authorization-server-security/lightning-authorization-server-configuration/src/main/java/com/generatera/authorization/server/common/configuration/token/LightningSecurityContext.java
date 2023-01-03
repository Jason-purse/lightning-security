package com.generatera.authorization.server.common.configuration.token;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 9:38
 * @Description 安全上下文 ...
 */
public interface LightningSecurityContext {

    /**
     * 认证信息
     */
    Authentication getAuthentication();

    /**
     * 提供者上下文
     */
    ProviderContext getProviderContext();


    TokenSettings getTokenSettings();

    /**
     * 需要生成的 token type
     */
    LightningToken.TokenType getTokenType();

    static LightningSecurityContext of(LightningToken.TokenType tokenType,
                                       Authentication authentication,
                                       ProviderContext providerContext,
                                       TokenSettings tokenSettings) {

        return new DefaultLightningSecurityContext(tokenType,
                authentication,
                providerContext,
                tokenSettings
        );
    }
}

@AllArgsConstructor
class DefaultLightningSecurityContext implements LightningSecurityContext {


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
