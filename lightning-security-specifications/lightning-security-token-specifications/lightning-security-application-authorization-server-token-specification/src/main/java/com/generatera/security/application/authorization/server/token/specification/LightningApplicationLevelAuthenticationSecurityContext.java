package com.generatera.security.application.authorization.server.token.specification;

import com.generatera.security.authorization.server.specification.TokenSettings;
import com.generatera.security.authorization.server.specification.endpoints.provider.ProviderContext;
import com.generatera.security.server.token.specification.LightningAuthorizationServerSecurityContext;
import com.generatera.security.server.token.specification.LightningTokenType;
import org.springframework.security.core.Authentication;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 11:33
 * @Description Lightning authentication Security Context
 */
public interface LightningApplicationLevelAuthenticationSecurityContext extends LightningAuthorizationServerSecurityContext {

    public static LightningApplicationLevelAuthenticationSecurityContext of(
            LightningAuthorizationServerSecurityContext securityContext
    ) {
        return new DefaultLightningApplicationLevelAuthenticationSecurityContext(securityContext);
    }
}

class DefaultLightningApplicationLevelAuthenticationSecurityContext implements LightningApplicationLevelAuthenticationSecurityContext {
    private final LightningAuthorizationServerSecurityContext securityContext;

    public DefaultLightningApplicationLevelAuthenticationSecurityContext(LightningAuthorizationServerSecurityContext securityContext) {
        this.securityContext = securityContext;
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
    public LightningTokenType.LightningAuthenticationTokenType getTokenType() {
        return securityContext.getTokenType();
    }


    @Override
    public Authentication getAuthentication() {
        return securityContext.getAuthentication();
    }
}

