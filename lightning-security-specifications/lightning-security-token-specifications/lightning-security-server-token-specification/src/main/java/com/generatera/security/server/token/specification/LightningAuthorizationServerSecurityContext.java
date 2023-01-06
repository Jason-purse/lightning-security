package com.generatera.security.server.token.specification;

import com.generatera.security.authorization.server.specification.TokenSettings;
import com.generatera.security.authorization.server.specification.endpoints.provider.ProviderContext;
import com.generatera.security.server.token.specification.LightningTokenType.LightningAuthenticationTokenType;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 9:38
 * @Description 安全上下文 ...
 */
public interface LightningAuthorizationServerSecurityContext extends LightningSecurityContext {


    /**
     * 提供者上下文
     */
    ProviderContext getProviderContext();


    TokenSettings getTokenSettings();

    /**
     * 需要生成的 token type
     */
    LightningAuthenticationTokenType getTokenType();

    static LightningAuthorizationServerSecurityContext of(
            Authentication authentication,
            ProviderContext providerContext,
            TokenSettings tokenSettings,
            LightningAuthenticationTokenType tokenType) {

        return new DefaultLightningAuthorizationServerSecurityContext(
                authentication,
                providerContext,
                tokenSettings,
                tokenType
        );
    }
}

@AllArgsConstructor
class DefaultLightningAuthorizationServerSecurityContext implements LightningAuthorizationServerSecurityContext {


    private Authentication authentication;

    private ProviderContext providerContext;

    private TokenSettings tokenSettings;

    private LightningAuthenticationTokenType tokenType;

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

    @Override
    public LightningAuthenticationTokenType getTokenType() {
        return tokenType;
    }
}
