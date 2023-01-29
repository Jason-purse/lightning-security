package com.generatera.authorization.application.server.config.authentication;

import com.generatera.authorization.application.server.config.token.AppAuthServerForTokenAuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

public class LightningAppAuthServerDaoLoginAuthenticationProvider extends DaoAuthenticationProvider {
    /**
     * 默认是false
     */
    private final boolean isSeparation;

    public LightningAppAuthServerDaoLoginAuthenticationProvider(
           AppAuthServerForTokenAuthenticationProvider authenticationProvider,
           boolean isSeparation
    ){
        Assert.notNull(authenticationProvider,"authentication provider must not be null !!!");
        this.authAccessAuthenticationProvider = authenticationProvider;
        this.isSeparation = isSeparation;
    }

    public LightningAppAuthServerDaoLoginAuthenticationProvider(
            AppAuthServerForTokenAuthenticationProvider authenticationProvider
    ){
        this(authenticationProvider,false);
    }

    private AppAuthServerForTokenAuthenticationProvider authAccessAuthenticationProvider;

    public void setAuthAccessAuthenticationProvider(AppAuthServerForTokenAuthenticationProvider authAccessAuthenticationProvider) {
        Assert.notNull(authAccessAuthenticationProvider,"authentication provider must not be null !!!");
        this.authAccessAuthenticationProvider = authAccessAuthenticationProvider;
    }

    @Override
    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication, UserDetails user) {
        Authentication successAuthentication = super.createSuccessAuthentication(principal, authentication, user);
        if(isSeparation) {
            return authAccessAuthenticationProvider.authenticate(successAuthentication);
        }
        return successAuthentication;
    }
}
