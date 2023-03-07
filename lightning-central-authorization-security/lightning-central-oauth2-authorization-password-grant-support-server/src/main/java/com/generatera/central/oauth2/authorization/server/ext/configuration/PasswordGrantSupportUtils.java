package com.generatera.central.oauth2.authorization.server.ext.configuration;

import com.generatera.security.authorization.server.specification.util.HttpSecurityBuilderUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

/**
 * @author Sun.
 */
public class PasswordGrantSupportUtils {

    public static <H extends HttpSecurityBuilder<H>> ResourceOwnerPasswordAuthenticationConverter getResourceOwnerPasswordAuthenticationConverter(H builder) {

       return  HttpSecurityBuilderUtils.getBean(builder, ResourceOwnerPasswordAuthenticationConverter.class, OAuth2ResourceOwnerPasswordAuthenticationConverter::new);
    }

    public static <H extends HttpSecurityBuilder<H>> ResourceOwnerPasswordAuthenticationProvider getResourceOwnerPasswordAuthenticationProvider(H builder) {

        return HttpSecurityBuilderUtils.getBean(builder, ResourceOwnerPasswordAuthenticationProvider.class, () -> {
            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);
            OAuth2AuthorizationService authorizationService = builder.getSharedObject(OAuth2AuthorizationService.class);
            OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator = builder.getSharedObject(OAuth2TokenGenerator.class);
            // oauth2.0 resourceOwnerPassword authentication support
            return  new OAuth2ResourceOwnerPasswordAuthenticationProvider(authenticationManager, authorizationService, tokenGenerator);
        });
    }
}
