package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.server.common.configuration.authorization.DefaultLightningAuthorization;
import com.generatera.authorization.server.common.configuration.authorization.store.LightningAuthenticationTokenService;
import com.generatera.security.authorization.server.specification.TokenSettingsProvider;
import com.generatera.security.authorization.server.specification.components.token.LightningToken;
import com.generatera.security.authorization.server.specification.components.token.LightningToken.LightningAccessToken;
import com.generatera.security.authorization.server.specification.components.token.format.JwtExtClaimNames;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.converter.ClaimConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AuthTokenIntrospectionAuthenticationProvider implements AuthenticationProvider {
    private static final TypeDescriptor OBJECT_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(Object.class);
    private static final TypeDescriptor LIST_STRING_TYPE_DESCRIPTOR = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(String.class));
    private final LightningAuthenticationTokenService authorizationService;

    private final TokenSettingsProvider tokenSettingsProvider;

    public AuthTokenIntrospectionAuthenticationProvider(LightningAuthenticationTokenService authorizationService,
                                                        TokenSettingsProvider tokenSettingsProvider) {
        Assert.notNull(authorizationService, "authorizationService cannot be null");
        Assert.notNull(authorizationService, "tokenSettingsProvider cannot be null");
        this.authorizationService = authorizationService;
        this.tokenSettingsProvider = tokenSettingsProvider;
    }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        AuthTokenIntrospectionAuthenticationToken tokenIntrospectionAuthentication = (AuthTokenIntrospectionAuthenticationToken) authentication;
        DefaultLightningAuthorization authorization = this.authorizationService.findByToken(tokenIntrospectionAuthentication.getToken(), null);
        if (authorization == null) {
            return tokenIntrospectionAuthentication;
        } else {
            DefaultLightningAuthorization.Token<LightningToken> authorizedToken = authorization.getToken(tokenIntrospectionAuthentication.getToken());
            if (authorizedToken == null || !authorizedToken.isActive()) {
                // 无效token ,不需要进行撤销 ..
                return new AuthTokenIntrospectionAuthenticationToken(tokenIntrospectionAuthentication.getToken(),
                        // 这里加不加入 authorities 不重要 ..(因为最终通过token 解析授权信息)
                        new UsernamePasswordAuthenticationToken(
                                authorization.getPrincipal(), null
                        ), AuthTokenIntrospection.builder().build());
            } else {
                AuthTokenIntrospection tokenClaims = withActiveTokenClaims(authorizedToken);
                // 这里加不加入 authorities 不重要 ..(因为最终通过token 解析授权信息)
                return new AuthTokenIntrospectionAuthenticationToken(authorizedToken.getToken().getTokenValue(),
                        UsernamePasswordAuthenticationToken.unauthenticated(authorization.getPrincipal(), null),
                        tokenClaims);
            }
        }
    }

    public boolean supports(Class<?> authentication) {
        return AuthTokenIntrospectionAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private static AuthTokenIntrospection withActiveTokenClaims(DefaultLightningAuthorization.Token<LightningToken> authorizedToken) {
        AuthTokenIntrospection.Builder tokenClaims;
        if (!CollectionUtils.isEmpty(authorizedToken.getClaims())) {
            Map<String, Object> claims = convertClaimsIfNecessary(authorizedToken.getClaims());
            tokenClaims = AuthTokenIntrospection.withClaims(claims).active(true);
        } else {
            tokenClaims = AuthTokenIntrospection.builder(true);
        }

        LightningToken token = authorizedToken.getToken();
        if (token.getIssuedAt() != null) {
            tokenClaims.issuedAt(token.getIssuedAt());
        }

        if (token.getExpiresAt() != null) {
            tokenClaims.expiresAt(token.getExpiresAt());
        }

        if (LightningAccessToken.class.isAssignableFrom(token.getClass())) {
            LightningAccessToken accessToken = (LightningAccessToken) token;
            tokenClaims.tokenType(accessToken.getTokenValueType().value());
        }

        return tokenClaims.build();
    }

    private static Map<String, Object> convertClaimsIfNecessary(Map<String, Object> claims) {
        Map<String, Object> convertedClaims = new HashMap<>(claims);
        Object value = claims.get("iss");
        if (value != null && !(value instanceof URL)) {
            URL convertedValue = (URL) ClaimConversionService.getSharedInstance().convert(value, URL.class);
            if (convertedValue != null) {
                convertedClaims.put("iss", convertedValue);
            }
        }

        value = claims.get(JwtExtClaimNames.SCOPE_CLAIM);
        Object convertedValue;
        if (value != null && !(value instanceof List)) {
            convertedValue = ClaimConversionService.getSharedInstance().convert(value, OBJECT_TYPE_DESCRIPTOR, LIST_STRING_TYPE_DESCRIPTOR);
            if (convertedValue != null) {
                convertedClaims.put(JwtExtClaimNames.SCOPE_CLAIM, convertedValue);
            }
        }
        value = claims.get(JwtExtClaimNames.SCOPE_SHORT_CLAIM);
        if (value != null && !(value instanceof List)) {
            convertedValue = ClaimConversionService.getSharedInstance().convert(value, OBJECT_TYPE_DESCRIPTOR, LIST_STRING_TYPE_DESCRIPTOR);
            if (convertedValue != null) {
                convertedClaims.put(JwtExtClaimNames.SCOPE_SHORT_CLAIM, convertedValue);
            }
        }

        value = claims.get(JwtExtClaimNames.AUTHORITIES_CLAIM);
        if (value != null && !(value instanceof List)) {
            convertedValue = ClaimConversionService.getSharedInstance().convert(value, OBJECT_TYPE_DESCRIPTOR, LIST_STRING_TYPE_DESCRIPTOR);
            if (convertedValue != null) {
                convertedClaims.put(JwtExtClaimNames.AUTHORITIES_CLAIM, convertedValue);
            }
        }

        value = claims.get("aud");
        if (value != null && !(value instanceof List)) {
            convertedValue = ClaimConversionService.getSharedInstance().convert(value, OBJECT_TYPE_DESCRIPTOR, LIST_STRING_TYPE_DESCRIPTOR);
            if (convertedValue != null) {
                convertedClaims.put("aud", convertedValue);
            }
        }

        return convertedClaims;
    }
}
