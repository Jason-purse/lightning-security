package com.generatera.security.authorization.server.specification.components.token;

import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningTokenValueFormat;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningTokenValueType;
import com.generatera.security.authorization.server.specification.components.token.format.JwtExtClaimNames;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author FLJ
 * @date 2023/1/5
 * @time 15:43
 * @Description Lightning Token(会出现很多种)
 */
public interface LightningToken {

    String getTokenValue();

    @Nullable
    default Instant getIssuedAt() {
        return null;
    }

    @Nullable
    default Instant getExpiresAt() {
        return null;
    }

    default Class<? extends LightningToken> getTokenClass() {
        return null;
    }


    interface LightningAccessToken extends LightningToken {

        LightningTokenValueType getTokenValueType();

        LightningTokenValueFormat getTokenValueFormat();

        default Map<String, Object> serialize() {
            return new LinkedHashMap<>() {
                {
                    put(JwtExtClaimNames.ACCESS_TOKEN_CLAIM, new LinkedHashMap<>() {{
                        put(JwtExtClaimNames.TOKEN_VALUE_TYPE_CLAIM, getTokenValueType().value());
                        put(JwtExtClaimNames.TOKEN_VALUE_FORMAT_CLAIM, getTokenValueFormat().value());
                        put(JwtExtClaimNames.TOKEN_VALUE_CLAIM, getTokenValue());
                    }});

                }
            };
        }

        ;

        @Override
        default Class<? extends LightningToken> getTokenClass() {
            return LightningAccessToken.class;
        }
    }


    interface LightningRefreshToken extends LightningToken {

        default Map<String, Object> serialize() {
            return new LinkedHashMap<>() {
                {
                    put(JwtExtClaimNames.REFRESH_TOKEN_CLAIM, new LinkedHashMap<>() {{
                        put(JwtExtClaimNames.TOKEN_VALUE_CLAIM, getTokenValue());
                    }});
                }
            };
        }

        ;

        @Override
        default Class<? extends LightningToken> getTokenClass() {
            return LightningRefreshToken.class;
        }
    }

    /**
     * 默认 抽象 Token
     */
    abstract class DefaultAbstractToken implements LightningToken, Serializable {

        @Serial
        private static final long serialVersionUID = 570L;

        private final String tokenValue;

        private final Instant issuedAt;

        private final Instant expiresAt;


        protected DefaultAbstractToken(String tokenValue) {
            this(tokenValue, null, null);
        }

        protected DefaultAbstractToken(String tokenValue, @Nullable Instant issuedAt, @Nullable Instant expiresAt) {
            Assert.hasText(tokenValue, "tokenValue cannot be empty");
            if (issuedAt != null && expiresAt != null) {
                Assert.isTrue(expiresAt.isAfter(issuedAt), "expiresAt must be after issuedAt");
            }

            this.tokenValue = tokenValue;
            this.issuedAt = issuedAt;
            this.expiresAt = expiresAt;
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
            return expiresAt;
        }
    }

    /**
     * 稍微复杂一点的,区分 {@code PlainToken},为了获取 {@code LightningTokenValueType} ..
     * <p>
     * 目前{@code LightningAccessToken} 或者 {@code LightningRefreshToken} 都继承于这个Token ..
     * <p>
     * 在自签名的情况下,访问Token 最终是 {@code LightningJwt} 但不是一个 LightningAccessToken,
     * 但是在某些情况下,我们可能需要 {@code LightningTokenValueType} - 那么我们需要强转到这个类型上 ..
     */
    class ComplexToken extends DefaultAbstractToken {

        private final LightningTokenValueType tokenValueType;

        public ComplexToken(LightningTokenValueType tokenValueType, String tokenValue, Instant issuedAt, Instant expiresAt) {

            super(tokenValue, issuedAt, assertExpiredAt(expiresAt));
            Assert.notNull(tokenValueType, "tokenValueType must not be null !!!");
            this.tokenValueType = tokenValueType;
        }

        private static Instant assertExpiredAt(Instant expiresAt) {
            Assert.notNull(expiresAt, "expiredAt cannot be empty");
            return expiresAt;
        }

        public LightningTokenValueType getTokenValueType() {
            return tokenValueType;
        }
    }

    /**
     * 简单token ..
     */
    abstract class PlainToken extends DefaultAbstractToken {

        protected PlainToken(String tokenValue) {
            super(tokenValue);
        }

        protected PlainToken(String tokenValue, Instant issuedAt, Instant expiresAt) {
            super(tokenValue, issuedAt, expiresAt);
        }
    }
}