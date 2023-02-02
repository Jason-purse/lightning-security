package com.generatera.oauth2.resource.server.config;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.generatera.oauth2.resource.server.config.token.LightningOAuth2UserPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;

import java.util.Collection;
import java.util.Map;

/**
 * @author FLJ
 * @date 2023/1/13
 * @time 10:02
 * @Description   opaque token 省查之后的 user principal ..
 *
 * 这是默认实现,一般来说,应用需要提供自己的 ...
 */
public class LightningOpaqueOAuth2UserPrincipal implements LightningOAuth2UserPrincipal {

    private final OAuth2IntrospectionAuthenticatedPrincipal authenticatedPrincipal;
    public LightningOpaqueOAuth2UserPrincipal(OAuth2IntrospectionAuthenticatedPrincipal authenticatedPrincipal) {
        this.authenticatedPrincipal = authenticatedPrincipal;
    }

    @Override
    public <A> A getAttribute(String name) {
        return authenticatedPrincipal.getAttribute(name);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return authenticatedPrincipal.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authenticatedPrincipal.getAuthorities();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    @JsonGetter
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
       return true;
    }

    @Override
    public boolean isEnabled() {
        return authenticatedPrincipal.isActive();
    }

    @Override
    @JsonGetter
    public String getName() {
        return authenticatedPrincipal.getName();
    }
}
