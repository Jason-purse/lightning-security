package com.generatera.authorization.application.server.oauth2.login.config.token;

import org.jetbrains.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;

/**
 *
 */
public class OAuth2LoginExtAuthenticationToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 570L;
    private OAuth2User principal;
    private ClientRegistration clientRegistration;

    private OAuth2AccessToken accessToken;
    private OAuth2RefreshToken refreshToken;

    public OAuth2LoginExtAuthenticationToken(ClientRegistration clientRegistration) {
        super(Collections.emptyList());
        Assert.notNull(clientRegistration, "clientRegistration cannot be null");
        this.clientRegistration = clientRegistration;
        this.setAuthenticated(false);
    }

    public OAuth2LoginExtAuthenticationToken(ClientRegistration clientRegistration, OAuth2AuthorizationExchange authorizationExchange, OAuth2User principal, Collection<? extends GrantedAuthority> authorities, OAuth2AccessToken accessToken) {
        this(clientRegistration, principal, authorities, accessToken, (OAuth2RefreshToken)null);
    }

    public OAuth2LoginExtAuthenticationToken(ClientRegistration clientRegistration, OAuth2User principal, Collection<? extends GrantedAuthority> authorities, OAuth2AccessToken accessToken, @Nullable OAuth2RefreshToken refreshToken) {
        super(authorities);
        Assert.notNull(clientRegistration, "clientRegistration cannot be null");
        Assert.notNull(principal, "principal cannot be null");
        Assert.notNull(accessToken, "accessToken cannot be null");
        this.clientRegistration = clientRegistration;
        this.principal = principal;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.setAuthenticated(true);
    }

    public OAuth2User getPrincipal() {
        return this.principal;
    }

    public Object getCredentials() {
        return "";
    }

    public ClientRegistration getClientRegistration() {
        return this.clientRegistration;
    }


    public OAuth2AccessToken getAccessToken() {
        return this.accessToken;
    }

    @Nullable
    public OAuth2RefreshToken getRefreshToken() {
        return this.refreshToken;
    }
}
