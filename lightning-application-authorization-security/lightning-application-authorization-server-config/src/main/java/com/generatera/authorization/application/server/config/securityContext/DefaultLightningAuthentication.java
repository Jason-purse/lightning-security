package com.generatera.authorization.application.server.config.securityContext;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;

public abstract class DefaultLightningAuthentication implements LightningAuthentication {
    
    private final Authentication authentication;

    public DefaultLightningAuthentication(Authentication authentication) {
        Assert.notNull(authentication,"authentication must not be null !!!");
        this.authentication = authentication;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authentication.getAuthorities();
    }

    @Override
    public Object getCredentials() {
        return authentication.getCredentials();
    }

    @Override
    public Object getDetails() {
        return authentication.getDetails();
    }

    @Override
    public Object getPrincipal() {
        return authentication.getPrincipal();
    }

    @Override
    public boolean isAuthenticated() {
        return authentication.isAuthenticated();
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        authentication.setAuthenticated(isAuthenticated);
    }

    @Override
    public String getName() {
        return authentication.getName();
    }
}
