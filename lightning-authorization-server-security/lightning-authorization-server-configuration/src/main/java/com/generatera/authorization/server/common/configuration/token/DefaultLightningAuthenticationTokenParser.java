package com.generatera.authorization.server.common.configuration.token;

import com.generatera.authorization.server.common.configuration.token.customizer.jwt.DefaultLightningJwtDecoder;
import com.generatera.authorization.server.common.configuration.token.customizer.jwt.LightningJwt;
import com.generatera.authorization.server.common.configuration.token.customizer.jwt.LightningJwtDecoder;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

/**
 * 支持access token / refresh token 以及系统扩展token
 */
public class DefaultLightningAuthenticationTokenParser implements LightningAuthenticationTokenParser {

    private final LightningJwtDecoder jwtDecoder;

    public DefaultLightningAuthenticationTokenParser(JWKSource<SecurityContext> source) {
        this.jwtDecoder = new DefaultLightningJwtDecoder(source);
    }
    @Override
    public LightningAuthenticationToken parse(LightningAuthenticationParseContext parseContext) {
        LightningJwt decode = jwtDecoder.decode(parseContext.getTokenValue());

        LightningToken token =
                LightningToken.token(decode.getTokenValue(), decode.getTokenType(), decode.getIssuedAt(), decode.getExpiresAt());
        if (token.getTokenType() == LightningToken.TokenType.ACCESS_TOKEN_TYPE) {
           return LightningAuthenticationToken.of(token,null);
        }
        else if(token.getTokenType() == LightningToken.TokenType.REFRESH_TOKEN_TYPE) {
            return LightningAuthenticationToken.of(null,token);
        }
        else {
            return LightningAuthenticationToken.of(token);
        }
    }
}
