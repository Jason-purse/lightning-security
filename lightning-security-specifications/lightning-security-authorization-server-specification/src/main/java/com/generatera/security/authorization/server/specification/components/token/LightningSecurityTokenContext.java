package com.generatera.security.authorization.server.specification.components.token;

import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.TokenIssueFormat;
import com.generatera.security.authorization.server.specification.TokenSettings;
import com.generatera.security.authorization.server.specification.components.provider.ProviderContext;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningAuthenticationTokenType;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningTokenValueType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.Authentication;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 9:38
 * @Description 安全上下文 ...
 */
public interface LightningSecurityTokenContext {

    /**
     * 认证信息
     */
    Authentication getAuthentication();

    ProviderContext getProviderContext();

    TokenSettings getTokenSettings();

    TokenIssueFormat getTokenFormat();

    LightningTokenValueType getTokenValueType();

    LightningAuthenticationTokenType getTokenType();

    LightningUserPrincipal getPrincipal();



    public static LightningSecurityTokenContext of(
            Authentication authentication,
            ProviderContext providerContext,
            TokenSettings tokenSettings,
            TokenIssueFormat tokenIssueFormat,
            LightningTokenValueType tokenValueType,
            LightningAuthenticationTokenType tokenType,
            LightningUserPrincipal principal
    ) {
        return new DefaultLightningSecurityTokenContext(
                authentication,
                providerContext,
                tokenSettings,
                tokenIssueFormat,
                tokenValueType,
                tokenType,
                principal
        );
    }
}
@Data
@AllArgsConstructor

class DefaultLightningSecurityTokenContext implements LightningSecurityTokenContext {

    private Authentication authentication;

    private ProviderContext providerContext;

    private TokenSettings tokenSettings;

    private TokenIssueFormat tokenIssueFormat;

    private LightningTokenValueType tokenValueType;

    private LightningAuthenticationTokenType tokenType;

    private LightningUserPrincipal principal;

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
    public TokenIssueFormat getTokenFormat() {
        return tokenIssueFormat;
    }

    @Override
    public LightningTokenValueType getTokenValueType() {
        return tokenValueType;
    }

    public LightningUserPrincipal getPrincipal() {
        return principal;
    }
}