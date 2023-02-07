package com.generatera.oauth2.resource.server.config.token;

import com.generatera.security.authorization.server.specification.components.token.format.jwt.LightningJwt;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

class LightningJwtGrantAuthorityMapperAdapter implements Converter<Jwt, Collection<GrantedAuthority>> {
    
    private final LightningJwtGrantAuthorityMapper jwtGrantAuthorityMapper;
    public LightningJwtGrantAuthorityMapperAdapter(LightningJwtGrantAuthorityMapper jwtGrantAuthorityMapper) {
        this.jwtGrantAuthorityMapper = jwtGrantAuthorityMapper;
    }
    
    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        LightningJwt.Builder builder = LightningJwt.withTokenValue(source.getTokenValue());
        builder.headers(headers -> headers.putAll(source.getHeaders()));
        LightningJwt lightningJwt = builder.claims(claims -> claims.putAll(source.getClaims())).build();
        return  jwtGrantAuthorityMapper.convert(lightningJwt);
    }
}