package com.generatera.resource.server.specification.token.jwt.config;

import com.generatera.resource.server.config.token.AbstractComplexTokenAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

public class JwtTokenAuthenticationToken extends AbstractComplexTokenAuthenticationToken<LightningJwt> {

    private static final long serialVersionUID = 570L;
    private final String name;

    public JwtTokenAuthenticationToken(LightningJwt jwt) {
        super(jwt);
        this.name = jwt.getSubject();
    }

    public JwtTokenAuthenticationToken(LightningJwt jwt, Collection<? extends GrantedAuthority> authorities) {
        super(jwt, authorities);
        this.setAuthenticated(true);
        this.name = jwt.getSubject();
    }

    public JwtTokenAuthenticationToken(LightningJwt jwt, Collection<? extends GrantedAuthority> authorities, String name) {
        super(jwt, authorities);
        this.setAuthenticated(true);
        this.name = name;
    }

    public Map<String, Object> getTokenAttributes() {
        return this.getToken().getClaims();
    }

    public String getName() {
        return this.name;
    }
}
