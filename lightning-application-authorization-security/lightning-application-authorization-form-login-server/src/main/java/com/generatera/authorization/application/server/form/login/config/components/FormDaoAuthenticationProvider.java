package com.generatera.authorization.application.server.form.login.config.components;

import com.generatera.authorization.application.server.config.token.AuthAccessTokenAuthenticationToken;
import com.generatera.authorization.application.server.config.token.LightningDaoAuthenticationProvider;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

public class FormDaoAuthenticationProvider implements LightningDaoAuthenticationProvider {
    private final DaoAuthenticationProvider authenticationProvider;

    public FormDaoAuthenticationProvider(DaoAuthenticationProvider provider) {
        Assert.notNull(provider, "provider must not be null !!!");
        this.authenticationProvider = provider;
    }

    @Override
    public Authentication authenticate(AuthAccessTokenAuthenticationToken authentication) {
        Authentication realAuthentication = authentication.getAuthentication();
        if (realAuthentication instanceof UsernamePasswordAuthenticationToken) {
            return authenticationProvider.authenticate(realAuthentication);
        }
        return null;
    }
}
