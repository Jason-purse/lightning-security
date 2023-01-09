package com.generatera.security.authorization.server.specification.components.token.format.jwt;

import com.generatera.security.authorization.server.specification.components.token.format.jwt.jose.Jwks;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.jetbrains.annotations.Nullable;

/**
 * @author FLJ
 * @date 2023/1/6
 * @time 14:15
 * @Description JWKSource 提供器
 */
public class JWKSourceProvider {

    private final JWKSource<SecurityContext> source;
    public JWKSourceProvider() {
        this.source = Jwks.defaultRandomJwkSource();
    }

    @Nullable
    public  JWKSource<SecurityContext> getJWKSource() {
        return source;
    }



}
