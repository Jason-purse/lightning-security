package com.generatera.authorization.server.common.configuration.token;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 9:29
 * @Description 生成Token
 */
public interface LightningToken {

    String getTokenValue();

    Instant getIssuedAt();

    Instant getExpiresAt();

    TokenType getTokenType();

    String ACCESS_TOKEN = "ACCESS_TOKEN";

    String REFRESH_TOKEN = "REFRESH_TOKEN";

    interface AccessToken extends LightningToken {

    }

    interface RefreshToken extends LightningToken {

    }


    static AccessToken accessToken(
            String tokenValue,
            Instant issuedAt,
            Instant expiresAt) {
        return new DefaultAccessToken(tokenValue, issuedAt, expiresAt);
    }

    static RefreshToken refreshToken(
            String tokenValue,
            Instant issuedAt,
            Instant expiresAt
    ) {
        return new DefaultRefreshToken(tokenValue, issuedAt, expiresAt);
    }


    @AllArgsConstructor
    class TokenType {

        @Getter
        private String tokenTypeString;

        public static TokenType ACCESS_TOKEN_TYPE = new TokenType(ACCESS_TOKEN);

        public static TokenType REFRESH_TOKEN_TYPE = new TokenType(REFRESH_TOKEN);
    }
}

@AllArgsConstructor
class DefaultLightningToken implements LightningToken {

    private String tokenValue;

    private Instant issuedAt;

    private Instant expiredAt;

    private TokenType tokenType;

    @Override
    public String getTokenValue() {
        return tokenValue;
    }

    @Override
    public Instant getIssuedAt() {
        return issuedAt;
    }

    @Override
    public Instant getExpiresAt() {
        return expiredAt;
    }

    @Override
    public TokenType getTokenType() {
        return tokenType;
    }
}

class DefaultAccessToken extends DefaultLightningToken implements LightningToken.AccessToken {

    public DefaultAccessToken(String tokenValue, Instant issuedAt, Instant expiredAt) {
        super(tokenValue, issuedAt, expiredAt, TokenType.ACCESS_TOKEN_TYPE);
    }
}

class DefaultRefreshToken extends DefaultLightningToken implements LightningToken.RefreshToken {

    public DefaultRefreshToken(String tokenValue, Instant issuedAt, Instant expiredAt) {
        super(tokenValue, issuedAt, expiredAt, TokenType.REFRESH_TOKEN_TYPE);
    }
}

