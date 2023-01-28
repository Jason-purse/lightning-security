package com.generatera.authorization.application.server.config.token;

import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
/**
 * @author FLJ
 * @date 2023/1/28
 * @time 12:45
 * @Description token 审查的认证token 实体 ..
 */
public class AuthTokenIntrospectionAuthenticationToken extends AbstractAuthenticationToken {

    private final String token;
    private final Authentication principal;
    private final String tokenTypeHint;
    private final Map<String, Object> additionalParameters;
    private final AuthTokenIntrospection tokenClaims;

    public AuthTokenIntrospectionAuthenticationToken(String token, Authentication principal, @Nullable String tokenTypeHint, @Nullable Map<String, Object> additionalParameters) {
        super(Collections.emptyList());
        Assert.hasText(token, "token cannot be empty");
        Assert.notNull(principal, "clientPrincipal cannot be null");
        this.token = token;
        this.principal = principal;
        this.tokenTypeHint = tokenTypeHint;
        this.additionalParameters = Collections.unmodifiableMap((additionalParameters != null ? new HashMap<>(additionalParameters) : Collections.emptyMap()));
        this.tokenClaims = AuthTokenIntrospection.builder().build();
    }

    public AuthTokenIntrospectionAuthenticationToken(String token, Authentication principal, AuthTokenIntrospection tokenClaims) {
        super(Collections.emptyList());
        Assert.hasText(token, "token cannot be empty");
        Assert.notNull(principal, "principal cannot be null");
        Assert.notNull(tokenClaims, "tokenClaims cannot be null");
        this.token = token;
        this.principal = principal;
        this.tokenTypeHint = null;
        this.additionalParameters = Collections.emptyMap();
        this.tokenClaims = tokenClaims;
        this.setAuthenticated(true);
    }

    public Object getPrincipal() {
        return this.principal;
    }

    public Object getCredentials() {
        return "";
    }

    public String getToken() {
        return this.token;
    }

    @Nullable
    public String getTokenTypeHint() {
        return this.tokenTypeHint;
    }

    public Map<String, Object> getAdditionalParameters() {
        return this.additionalParameters;
    }

    public AuthTokenIntrospection getTokenClaims() {
        return this.tokenClaims;
    }


}