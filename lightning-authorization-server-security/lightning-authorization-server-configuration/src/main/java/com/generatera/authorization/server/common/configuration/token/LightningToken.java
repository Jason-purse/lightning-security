package com.generatera.authorization.server.common.configuration.token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.Assert;

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

    static LightningToken token(
            String tokenValue,
            TokenType tokenType,
            Instant issuedAt,
            Instant expiresAt
    ) {
        if(tokenType == TokenType.ACCESS_TOKEN_TYPE) {
            return accessToken(tokenValue,issuedAt,expiresAt);
        }
        else if(tokenType == TokenType.REFRESH_TOKEN_TYPE) {
            return refreshToken(tokenValue,issuedAt,expiresAt);
        }
        return new DefaultLightningToken(
                tokenValue,
                issuedAt,
                expiresAt,
                tokenType
        );
    }


    @AllArgsConstructor
    class TokenType {

        @Getter
        private String tokenTypeString;

        public static TokenType ACCESS_TOKEN_TYPE = new TokenType(ACCESS_TOKEN);

        public static TokenType REFRESH_TOKEN_TYPE = new TokenType(REFRESH_TOKEN);

        public static TokenType of(String tokenTypeString) {
            return new TokenType(tokenTypeString);
        }
    }
}

class DefaultLightningToken implements LightningToken {

    private String tokenValue;

    private Instant issuedAt;

    private Instant expiredAt;

    private TokenType tokenType;

    public DefaultLightningToken(String tokenValue, Instant issuedAt, Instant expiredAt,TokenType tokenType) {

        Assert.notNull(tokenType,"tokenType must not be null !!!!");
        Assert.notNull(issuedAt,"issuedAt must not be null !!!!");
        Assert.notNull(expiredAt,"expiredAt must not be null !!!!");
        Assert.notNull(tokenValue,"tokenValue must not be null !!!!");


        this.tokenType = tokenType;
        this.issuedAt = issuedAt;
        this.expiredAt = expiredAt;
        this.tokenValue = tokenValue;
    }

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

