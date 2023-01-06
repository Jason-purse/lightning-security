package com.generatera.authorization.application.server.oauth2.login.config.user;

import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.*;

public class OidcUserPrincipal  implements OidcUser, LightningUserPrincipal {
    private final OidcUser user;
    public OidcUserPrincipal(OidcUser user) {
        this.user = user;
    }

    @Override
    public Map<String, Object> getClaims() {
        return user.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return user.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return user.getIdToken();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return user.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getAuthorities();
    }

    @Override
    public List<String> getAuthoritiesForString() {
        return Objects.requireNonNullElse(getAuthorities(), Collections.<GrantedAuthority>emptyList())
                .stream().map(GrantedAuthority::getAuthority)
                .toList();
    }

    @Override
    public String getName() {
        return user.getName();
    }
}
