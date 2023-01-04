package com.generatera.authorization.server.common.configuration.token;

import com.generatera.authorization.server.common.configuration.ext.oauth2.provider.ProviderContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.Authentication;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 11:33
 * @Description Lightning authentication Security Context
 */
public interface LightningAuthenticationSecurityContext {

    Authentication getAuthentication();

    ProviderContext getProviderContext();

    TokenSettings getTokenSettings();


    public static LightningAuthenticationSecurityContext of(
            Authentication authentication,
            ProviderContext context,
            TokenSettings tokenSettings
    ) {
        return new DefaultLightningAuthenticationSecurityContext(
                authentication,
                context,
                tokenSettings
        );
    }
}

@Getter
@AllArgsConstructor
class DefaultLightningAuthenticationSecurityContext implements LightningAuthenticationSecurityContext {

    private Authentication authentication;

    private ProviderContext providerContext;

    private TokenSettings tokenSettings;
}
