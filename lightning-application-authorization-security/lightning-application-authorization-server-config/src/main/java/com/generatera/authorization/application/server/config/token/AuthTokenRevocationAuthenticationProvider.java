package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.application.server.config.authorization.DefaultLightningAuthorization;
import com.generatera.authorization.application.server.config.authorization.store.LightningAuthenticationTokenService;
import com.generatera.security.authorization.server.specification.components.token.LightningToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

/**
 * @author FLJ
 * @date 2023/1/28
 * @time 13:53
 * @Description 认证 token 撤销 认证提供器 ..
 */
public final class AuthTokenRevocationAuthenticationProvider implements AuthenticationProvider {
    private final LightningAuthenticationTokenService authorizationService;

    public AuthTokenRevocationAuthenticationProvider(LightningAuthenticationTokenService authorizationService) {
        Assert.notNull(authorizationService, "authorizationService cannot be null");
        this.authorizationService = authorizationService;
    }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        AuthTokenRevocationAuthenticationToken tokenRevocationAuthentication = (AuthTokenRevocationAuthenticationToken) authentication;
        // 只要是token 都能撤销 ..
        DefaultLightningAuthorization authorization = this.authorizationService.findByToken(tokenRevocationAuthentication.getToken(), null);
        if (authorization == null) {
            return tokenRevocationAuthentication;
        } else {
            DefaultLightningAuthorization.Token<LightningToken> token = authorization.getToken(tokenRevocationAuthentication.getToken());
            authorization = AuthAuthenticationProviderUtils.invalidate(authorization, token.getToken());
            this.authorizationService.save(authorization);
            return new AuthTokenRevocationAuthenticationToken(token.getToken(),
                    new UsernamePasswordAuthenticationToken(
                            // 丢掉凭证 ..
                            authorization.getPrincipal(), null
                    ));
        }
    }

    public boolean supports(Class<?> authentication) {
        return AuthTokenRevocationAuthenticationToken.class.isAssignableFrom(authentication);
    }
}