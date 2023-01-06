package com.generatera.security.server.token.specification.format.jwt;

import com.generatera.security.server.token.specification.AbstractOAuthTokenAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Transient;

import java.util.Collection;
import java.util.Map;

@Transient
public class JwtTokenAuthentication extends AbstractOAuthTokenAuthenticationToken<LightningJwt> {
    private static final long serialVersionUID = 570L;
    private final String name;

    public JwtTokenAuthentication(LightningJwt jwt) {
        super(jwt);
        this.name = jwt.getSubject();
    }

    public JwtTokenAuthentication(LightningJwt jwt, Collection<? extends GrantedAuthority> authorities) {
        super(jwt, authorities);
        this.setAuthenticated(true);
        this.name = jwt.getSubject();
    }

    public JwtTokenAuthentication(LightningJwt jwt, Collection<? extends GrantedAuthority> authorities, String name) {
        super(jwt, authorities);
        this.setAuthenticated(true);
        this.name = name;
    }

    public Map<String, Object> getTokenAttributes() {
        return ((LightningJwt)this.getToken()).getClaims();
    }

    public String getName() {
        return this.name;
    }
}