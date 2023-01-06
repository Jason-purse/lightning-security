package com.generatera.resource.server.config.token;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Map;

public abstract class AbstractComplexTokenAuthenticationToken <T extends LightningToken.ComplexToken> implements LightningAuthenticationToken {

    private static final long serialVersionUID = 570L;
    private final Object principal;
    private final Object credentials;
    private final T token;

    protected AbstractComplexTokenAuthenticationToken(T token) {
        this(token, null);
    }


    protected AbstractComplexTokenAuthenticationToken(T token, Collection<? extends GrantedAuthority> authorities) {
        this(token, token, token, authorities);
    }

    protected AbstractComplexTokenAuthenticationToken(T token, Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        //super(authorities);
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
