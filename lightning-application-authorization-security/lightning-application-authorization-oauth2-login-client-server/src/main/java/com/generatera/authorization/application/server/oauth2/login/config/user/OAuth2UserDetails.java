package com.generatera.authorization.application.server.oauth2.login.config.user;

import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;

public class OAuth2UserDetails implements OAuth2User,LightningOAuth2UserDetails {

    private final OAuth2User oAuth2User;

    @Nullable
    private final Instant expiredAt;

    @Nullable
    private final Instant issuedAt;

    public OAuth2UserDetails(OAuth2User oAuth2User, @Nullable Instant issuedAt, @Nullable Instant expiredAt) {
        Assert.notNull(oAuth2User,"oauth2 user must not be null !!!");
        this.oAuth2User = oAuth2User;
        this.issuedAt = issuedAt;
        this.expiredAt = expiredAt;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return expiredAt == null ||   expiredAt.isAfter(Instant.now());
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oAuth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return oAuth2User.getName();
    }
}
