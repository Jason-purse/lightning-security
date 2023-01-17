package com.generatera.oauth2.resource.server.config.token;

import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.components.token.JwtClaimsToUserPrincipalMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.Collection;
import java.util.Map;

/**
 * @author FLJ
 * @date 2023/1/13
 * @time 10:25
 * @Description 主要是Lightning Oauth2 User的主体信息 ..
 *
 * 应用一般需要提供自己的 {@link JwtClaimsToUserPrincipalMapper}
 * 去提供应用级可用的 用户信息,以便 {@link com.generatera.security.authorization.server.specification.LightningUserContext}
 * 能够拿取到正确的用户信息 ...
 *
 * 或者jwt token的情况下 ,提供自己的 {@link LightningJwtAuthenticationConverter}
 * 或者 opaque token的情况下,提供自己的 {@link LightningOAuth2OpaqueTokenIntrospector} 或者
 * {@link JwtClaimsToUserPrincipalMapper}
 *
 * @see com.generatera.oauth2.resource.server.config.LightningJwtOAuth2UserPrincipal
 * @see com.generatera.oauth2.resource.server.config.LightningOpaqueOAuth2UserPrincipal
 * @see com.generatera.security.authorization.server.specification.LightningUserContext
 * @see LightningJwtAuthenticationConverter
 * @see LightningOAuth2OpaqueTokenIntrospector
 * @see JwtClaimsToUserPrincipalMapper
 */
public interface LightningOAuth2UserPrincipal extends LightningUserPrincipal,OAuth2AuthenticatedPrincipal {
    @Override
    Collection<? extends GrantedAuthority> getAuthorities();

    @Override
    String getPassword();

    @Override
    String getUsername();

    @Override
    boolean isAccountNonExpired();

    @Override
    boolean isAccountNonLocked();

    @Override
    boolean isCredentialsNonExpired();

    @Override
    boolean isEnabled();

    @Override
    <A> A getAttribute(String name);

    @Override
    Map<String, Object> getAttributes();

    @Override
    String getName();
}
