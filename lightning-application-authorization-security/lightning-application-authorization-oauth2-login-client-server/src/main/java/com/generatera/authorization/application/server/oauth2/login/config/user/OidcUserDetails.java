package com.generatera.authorization.application.server.oauth2.login.config.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.time.Instant;
import java.util.*;

/**
 * @author FLJ
 * @date 2023/1/12
 * @time 11:35
 * @Description 这是默认 实现 ...
 *
 * 当没有提供自己的LightningOAuth2UserService 时,使用它 ...
 */
public class OidcUserDetails implements OidcUser, LightningOAuth2UserDetails {
    private final OidcUser user;

    public OidcUserDetails(OidcUser user) {
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
    public String getPassword() {
        // 没有密码
        return "";
    }

    @Override
    public String getUsername() {
        return user.getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return Instant.now().isBefore(getExpiresAt());
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return getUsername();
    }
}
