package com.generatera.oauth2.resource.server.config.token;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Transient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
/**
 * @author FLJ
 * @date 2023/1/12
 * @time 16:54
 * @Description 表示资源服务器这边的BearerToken Authentication ...
 */
@Transient
public class LightningResourceBearerTokenAuthentication extends AbstractOAuth2TokenAuthenticationToken<OAuth2AccessToken> {

    private final Map<String, Object> attributes;

    public LightningResourceBearerTokenAuthentication(OAuth2AuthenticatedPrincipal principal, OAuth2AccessToken credentials, Collection<? extends GrantedAuthority> authorities) {
        super(credentials, principal, credentials, authorities);
        Assert.isTrue(credentials.getTokenType() == OAuth2AccessToken.TokenType.BEARER, "credentials must be a bearer token");
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<>(principal.getAttributes()));
        this.setAuthenticated(true);
    }

    public Map<String, Object> getTokenAttributes() {
        return this.attributes;
    }
}
