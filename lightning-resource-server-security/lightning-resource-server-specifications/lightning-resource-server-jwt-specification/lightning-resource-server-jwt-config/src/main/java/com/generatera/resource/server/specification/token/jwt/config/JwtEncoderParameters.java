package com.generatera.resource.server.specification.token.jwt.config;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public final class JwtEncoderParameters {
    private final JwsHeader jwsHeader;
    private final JwtClaimsSet claims;


    private JwtEncoderParameters(JwsHeader jwsHeader, JwtClaimsSet claims) {
        this.jwsHeader = jwsHeader;
        this.claims = claims;
    }

    public static JwtEncoderParameters from(JwtClaimsSet claims) {
        Assert.notNull(claims, "claims cannot be null");
        return new JwtEncoderParameters((JwsHeader)null, claims);
    }

    public static JwtEncoderParameters from(JwsHeader jwsHeader, JwtClaimsSet claims) {
        Assert.notNull(jwsHeader, "jwsHeader cannot be null");
        Assert.notNull(claims, "claims cannot be null");
        return new JwtEncoderParameters(jwsHeader, claims);
    }

    @Nullable
    public JwsHeader getJwsHeader() {
        return this.jwsHeader;
    }

    public JwtClaimsSet getClaims() {
        return this.claims;
    }

}