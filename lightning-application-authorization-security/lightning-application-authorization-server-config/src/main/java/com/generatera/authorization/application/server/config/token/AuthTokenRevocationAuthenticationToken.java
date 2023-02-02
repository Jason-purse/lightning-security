package com.generatera.authorization.application.server.config.token;

import com.generatera.security.authorization.server.specification.components.token.LightningToken;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import java.util.Collections;
/**
 * @author FLJ
 * @date 2023/1/28
 * @time 13:41
 * @Description token revocation
 */
public class AuthTokenRevocationAuthenticationToken extends AbstractAuthenticationToken {

    private final String token;
    private final Authentication principal;
    private final String tokenTypeHint;

    public AuthTokenRevocationAuthenticationToken(String token, @Nullable Authentication principal, @Nullable String tokenTypeHint) {
        super(Collections.emptyList());
        Assert.hasText(token, "token cannot be empty");
        this.token = token;
        this.principal = principal;
        this.tokenTypeHint = tokenTypeHint;
    }

    public AuthTokenRevocationAuthenticationToken(LightningToken revokedToken, Authentication principal) {
        super(Collections.emptyList());
        Assert.notNull(revokedToken, "revokedToken cannot be null");
        Assert.notNull(principal, "principal cannot be null");
        this.token = revokedToken.getTokenValue();
        this.principal = principal;
        this.tokenTypeHint = null;
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


}