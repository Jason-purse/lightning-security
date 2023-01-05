package com.generatera.resource.server.specification.token.jwt.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * @author FLJ
 * @date 2023/1/5
 * @time 16:01
 * @Description 将 Jwt 转换为 一个抽象的Token ...
 */
public class JwtAuthenticationConverter implements Converter<LightningJwt, AbstractAuthenticationToken> {
    private Converter<LightningJwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    private String principalClaimName = "sub";

    public JwtAuthenticationConverter() {
    }

    @NonNull
    public final AbstractAuthenticationToken convert(@NonNull LightningJwt jwt) {
        Collection<GrantedAuthority> authorities = this.extractAuthorities(jwt);
        String principalClaimValue = jwt.getClaimAsString(this.principalClaimName);
        return new JwtTokenAuthenticationToken(jwt, authorities, principalClaimValue);
    }

    /** @deprecated */
    @Deprecated
    protected Collection<GrantedAuthority> extractAuthorities(LightningJwt jwt) {
        return this.jwtGrantedAuthoritiesConverter.convert(jwt);
    }

    public void setJwtGrantedAuthoritiesConverter(Converter<LightningJwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter) {
        Assert.notNull(jwtGrantedAuthoritiesConverter, "jwtGrantedAuthoritiesConverter cannot be null");
        this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;
    }

    public void setPrincipalClaimName(String principalClaimName) {
        Assert.hasText(principalClaimName, "principalClaimName cannot be empty");
        this.principalClaimName = principalClaimName;
    }
}