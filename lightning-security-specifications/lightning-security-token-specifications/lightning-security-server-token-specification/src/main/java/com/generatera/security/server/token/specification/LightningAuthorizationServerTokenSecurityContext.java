package com.generatera.security.server.token.specification;

import com.generatera.security.authorization.server.specification.TokenSettings;
import com.generatera.security.authorization.server.specification.endpoints.provider.ProviderContext;
import com.generatera.security.server.token.specification.LightningTokenType.LightningAuthenticationTokenType;
import com.generatera.security.server.token.specification.LightningTokenType.LightningTokenValueType;
import com.generatera.security.server.token.specification.format.LightningTokenFormat;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;

/**
 * @author FLJ
 * @date 2023/1/6
 * @time 13:27
 * @Description LightningToken的上下文
 */
public interface LightningAuthorizationServerTokenSecurityContext extends LightningAuthorizationServerSecurityContext {


    LightningTokenFormat getTokenFormat();

    LightningTokenValueType getTokenValueType();

    static DefaultLightningAuthorizationServerTokenSecurityContext of(LightningAuthenticationTokenType tokenType,
                                                                      LightningTokenFormat tokenFormat,
                                                                      LightningTokenValueType tokenValueType,
                                                                      Authentication authentication,
                                                                      ProviderContext providerContext,
                                                                      TokenSettings tokenSettings) {

        return new DefaultLightningAuthorizationServerTokenSecurityContext(
                LightningAuthorizationServerSecurityContext.of(
                        authentication,
                        providerContext,
                        tokenSettings,
                        tokenType
                ),
                tokenFormat,
                tokenValueType
        );
    }

    static DefaultLightningAuthorizationServerTokenSecurityContext of(LightningAuthorizationServerSecurityContext context,
                                                                      LightningTokenFormat tokenFormat,
                                                                      LightningTokenValueType tokenValueType) {
        return new DefaultLightningAuthorizationServerTokenSecurityContext(context, tokenFormat,tokenValueType);
    }
}

@AllArgsConstructor
class DefaultLightningAuthorizationServerTokenSecurityContext implements LightningAuthorizationServerTokenSecurityContext {

    public LightningAuthorizationServerSecurityContext securityContext;

    private LightningTokenFormat tokenFormat;

    private LightningTokenValueType tokenValueType;

    @Override
    public LightningAuthenticationTokenType getTokenType() {
        return securityContext.getTokenType();
    }

    @Override
    public Authentication getAuthentication() {
        return securityContext.getAuthentication();
    }

    @Override
    public ProviderContext getProviderContext() {
        return securityContext.getProviderContext();
    }

    @Override
    public TokenSettings getTokenSettings() {
        return securityContext.getTokenSettings();
    }

    @Override
    public LightningTokenFormat getTokenFormat() {
        return tokenFormat;
    }

    @Override
    public LightningTokenValueType getTokenValueType() {
        return tokenValueType;
    }
}
