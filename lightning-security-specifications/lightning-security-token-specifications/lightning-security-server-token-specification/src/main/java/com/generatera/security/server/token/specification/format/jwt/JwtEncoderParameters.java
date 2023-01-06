package com.generatera.security.server.token.specification.format.jwt;

import com.generatera.security.server.token.specification.LightningTokenType.LightningAuthenticationTokenType;
import com.generatera.security.server.token.specification.LightningTokenType.LightningTokenValueType;
import com.generatera.security.server.token.specification.format.jwt.jose.JwsHeader;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;

public final class JwtEncoderParameters {
    private final JwsHeader jwsHeader;
    private final JwtClaimsSet claims;

    private final LightningAuthenticationTokenType tokenType;

    private final LightningTokenValueType tokenValueType;


    private JwtEncoderParameters(JwsHeader jwsHeader, JwtClaimsSet claims, LightningAuthenticationTokenType tokenType, LightningTokenValueType tokenValueType) {
        this.jwsHeader = jwsHeader;
        this.claims = claims;
        this.tokenType  = tokenType;
        this.tokenValueType = tokenValueType;
    }

    public static JwtEncoderParameters from(JwtClaimsSet claims,
                                            LightningAuthenticationTokenType tokenType, LightningTokenValueType tokenValueType) {
        Assert.notNull(claims, "claims cannot be null");
        return new JwtEncoderParameters((JwsHeader)null, claims,tokenType,tokenValueType);
    }

    public static JwtEncoderParameters from(JwsHeader jwsHeader, JwtClaimsSet claims,
                                            LightningAuthenticationTokenType tokenType, LightningTokenValueType tokenValueType) {
        Assert.notNull(jwsHeader, "jwsHeader cannot be null");
        Assert.notNull(claims, "claims cannot be null");
        return new JwtEncoderParameters(jwsHeader, claims,tokenType,tokenValueType);
    }

    @Nullable
    public JwsHeader getJwsHeader() {
        return this.jwsHeader;
    }

    public JwtClaimsSet getClaims() {
        return this.claims;
    }

    public LightningTokenValueType getTokenValueType() {
        return tokenValueType;
    }

    public LightningAuthenticationTokenType getTokenType() {
        return tokenType;
    }
}