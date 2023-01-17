package com.generatera.security.authorization.server.specification.components.token.format.jwt;

import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningAuthenticationTokenType;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningTokenValueType;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.jose.JwsHeader;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;
/**
 * @author FLJ
 * @date 2023/1/17
 * @time 10:28
 * @Description jwt 编码器需要的一些参数 ..
 *
 * 这里包括了额外的信息,例如 token 类型 / token value 类型..
 */
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