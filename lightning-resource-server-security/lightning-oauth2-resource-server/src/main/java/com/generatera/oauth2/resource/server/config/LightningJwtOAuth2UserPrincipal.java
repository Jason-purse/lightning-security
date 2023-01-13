package com.generatera.oauth2.resource.server.config;

import com.generatera.oauth2.resource.server.config.token.LightningOAuth2UserPrincipal;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.LightningJwt;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.*;

/**
 * @author FLJ
 * @date 2023/1/12
 * @time 11:36
 * @Description 这是 Jwt 令牌的默认实现 ...
 * <p>
 * 一般来说,应用需要加入自己的 jwt 转换到 LightningUserPrincipal(自己想要的) 形式上 ...
 * <p>
 * 这个只是默认实现,包装了Jwt ...
 */
public class LightningJwtOAuth2UserPrincipal implements LightningOAuth2UserPrincipal {

    private final Collection<? extends GrantedAuthority> authorities;

    private final LightningJwt jwt;

    public LightningJwtOAuth2UserPrincipal(Jwt jwt) {
        this.jwt = new LightningJwt(jwt.getTokenValue(),jwt.getIssuedAt(),jwt.getExpiresAt(),jwt.getHeaders(),jwt.getClaims());
        this.authorities = initAuthorities();
    }


    @NotNull
    private List<GrantedAuthority> initAuthorities() {
        return Optional
                .ofNullable(jwt.getClaimAsString("scope"))
                .map(ele -> ele.split(","))
                .map(ele -> {
                    List<GrantedAuthority> values = new LinkedList<>();
                    for (String s : ele) {
                        values.add(new SimpleGrantedAuthority(s));
                    }
                    return values;
                })
                .orElse(Collections.emptyList());
    }

    @Override
    public String getName() {
        return jwt.getSubject();
    }

    @Override
    public <A> A getAttribute(String name) {
        return jwt.getClaim(name);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return jwt.getClaims();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
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
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return Optional
                .ofNullable(jwt.getExpiresAt())
                .map(ele -> Instant.now().isBefore(ele))
                .orElse(Boolean.FALSE);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
