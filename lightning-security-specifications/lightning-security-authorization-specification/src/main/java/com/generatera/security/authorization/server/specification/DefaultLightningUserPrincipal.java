package com.generatera.security.authorization.server.specification;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
/**
 * @author FLJ
 * @date 2023/3/1
 * @time 12:00
 * @Description 默认 Lightning UserPrincipal 用户信息 ...
 */
public class DefaultLightningUserPrincipal implements LightningUserPrincipal {

    private final String password;
    private final String username;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public DefaultLightningUserPrincipal(
            String username,
            String password,
            boolean accountNonExpired,
            boolean accountNonLocked,
            boolean credentialsNonExpired,
            boolean enabled,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.password = password;
        this.username = username;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    public static DefaultLightningUserPrincipal authenticated(
              String username,
              String password,
              Collection<? extends GrantedAuthority> authorities
    ) {
        return new DefaultLightningUserPrincipal(
                username,
                password,
                true,
                true,
                true,
                true,
                authorities
        );
    }

    public static DefaultLightningUserPrincipal unauthenticated(
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities
    ) {
        return new DefaultLightningUserPrincipal(
                username,
                password,
                false,
                false,
                false,
                false,
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
