package com.generatera.security.server.token.specification;

import com.generatera.security.server.token.specification.LightningTokenType.LightningTokenValueType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

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

    interface LightningAccessToken extends LightningToken {

    }

    interface LightningRefreshToken extends LightningToken  {
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

    abstract class ComplexToken extends DefaultAbstractToken {

        private final LightningTokenValueType tokenValueType;

        protected ComplexToken(LightningTokenValueType tokenValueType,String tokenValue, Instant issuedAt, Instant expiresAt) {

            super(tokenValue, issuedAt, assertExpiredAt(expiresAt));
            Assert.notNull(tokenValueType,"tokenValueType must not be null !!!");
            this.tokenValueType = tokenValueType;
        }

        private static Instant assertExpiredAt(Instant expiresAt) {
            Assert.notNull(expiresAt,"expiredAt cannot be empty");
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