package com.generatera.resource.server.config.token;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

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

    /**
     * 复杂Token
     */
    abstract class ComplexToken implements LightningToken, Serializable {

        private static final long serialVersionUID = 570L;

        private final String tokenValue;

        private final Instant issuedAt;

        private final Instant expiresAt;

        protected ComplexToken(String tokenValue) {
            this(tokenValue, null, null);
        }

        protected ComplexToken(String tokenValue, @Nullable Instant issuedAt, @Nullable Instant expiresAt) {
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
}