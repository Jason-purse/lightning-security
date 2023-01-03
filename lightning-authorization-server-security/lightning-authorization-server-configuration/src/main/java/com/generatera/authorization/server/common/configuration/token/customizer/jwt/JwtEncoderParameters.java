package com.generatera.authorization.server.common.configuration.token.customizer.jwt;

import com.generatera.authorization.server.common.configuration.token.LightningToken;
import com.generatera.authorization.server.common.configuration.token.customizer.jwt.jose.JwsHeader;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;

public final class JwtEncoderParameters {
    private final JwsHeader jwsHeader;
    private final JwtClaimsSet claims;

    private final LightningToken.TokenType tokenType;

    private JwtEncoderParameters(LightningToken.TokenType tokenType,JwsHeader jwsHeader, JwtClaimsSet claims) {
        this.tokenType = tokenType;
        this.jwsHeader = jwsHeader;
        this.claims = claims;
    }

    public static JwtEncoderParameters from(JwtClaimsSet claims, LightningToken.TokenType tokenType) {
        Assert.notNull(claims, "claims cannot be null");
        return new JwtEncoderParameters(tokenType,(JwsHeader)null, claims);
    }

    public static JwtEncoderParameters from(JwsHeader jwsHeader, JwtClaimsSet claims, LightningToken.TokenType tokenType) {
        Assert.notNull(jwsHeader, "jwsHeader cannot be null");
        Assert.notNull(claims, "claims cannot be null");
        Assert.notNull(tokenType,"token type cannot be null");
        return new JwtEncoderParameters(tokenType,jwsHeader, claims);
    }

    @Nullable
    public JwsHeader getJwsHeader() {
        return this.jwsHeader;
    }

    public JwtClaimsSet getClaims() {
        return this.claims;
    }

    public LightningToken.TokenType getTokenType() {
        return tokenType;
    }
}