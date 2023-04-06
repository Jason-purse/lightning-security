package com.generatera.authorization.application.server.form.login.config.components;

import com.generatera.authorization.application.server.config.UsernamePasswordAuthenticationWithRequestToken;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

public class OptmizedDaoAuthenticationProvider extends DaoAuthenticationProvider {
    @Override
    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication, UserDetails user) {
        LightningUserDetailService userDetailsService = (LightningUserDetailService) getUserDetailsService();
        LightningUserPrincipal userPrincipal = (LightningUserPrincipal) principal;
        LightningUserPrincipal authenticatedUser = userDetailsService.mapAuthenticatedUser(userPrincipal);
        Assert.isTrue(authenticatedUser.isAuthenticated(), "LightningUserDetailService must provide an authenticated user convert result !!!");
        Authentication successAuthentication = super.createSuccessAuthentication(principal, authentication, user);
        return new UsernamePasswordAuthenticationWithRequestToken(
                successAuthentication,
                authentication
        );
    }
}
