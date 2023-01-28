package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.server.common.configuration.AuthorizationGrantType;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AuthRefreshTokenAuthenticationToken extends AuthAuthorizationGrantAuthenticationToken {
    private final String refreshToken;
    private final Set<String> scopes;

    public AuthRefreshTokenAuthenticationToken(String refreshToken, Authentication clientPrincipal, @Nullable Set<String> scopes, @Nullable Map<String, Object> additionalParameters) {
        super(AuthorizationGrantType.REFRESH_TOKEN, clientPrincipal, additionalParameters);
        Assert.hasText(refreshToken, "refreshToken cannot be empty");
        this.refreshToken = refreshToken;
        this.scopes = Collections.unmodifiableSet(scopes != null ? new HashSet<>(scopes) : Collections.emptySet());
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public Set<String> getScopes() {
        return this.scopes;
    }
}
