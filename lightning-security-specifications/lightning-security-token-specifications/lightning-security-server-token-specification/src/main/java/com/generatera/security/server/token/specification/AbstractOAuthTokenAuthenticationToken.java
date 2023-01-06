package com.generatera.security.server.token.specification;

import com.generatera.security.server.token.specification.LightningToken.ComplexToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Map;

public abstract class AbstractOAuthTokenAuthenticationToken<T extends ComplexToken> extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 570L;
    private Object principal;
    private Object credentials;
    private T token;

    protected AbstractOAuthTokenAuthenticationToken(T token) {
        this(token, (Collection)null);
    }

    protected AbstractOAuthTokenAuthenticationToken(T token, Collection<? extends GrantedAuthority> authorities) {
        this(token, token, token, authorities);
    }

    protected AbstractOAuthTokenAuthenticationToken(T token, Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        Assert.notNull(token, "token cannot be null");
        Assert.notNull(principal, "principal cannot be null");
        this.principal = principal;
        this.credentials = credentials;
        this.token = token;
    }

    public Object getPrincipal() {
        return this.principal;
    }

    public Object getCredentials() {
        return this.credentials;
    }

    public final T getToken() {
        return this.token;
    }

    public abstract Map<String, Object> getTokenAttributes();
}