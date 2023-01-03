package com.generatera.authorization.server.common.configuration.token.customizer.jwt;

import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 10:32
 * @Description Lighting Jwt (spring oauth2 jwt copy)
 */
public class DefaultLightningJwt implements LightningJwt {

    private String tokenValue;

    private Instant issuedAt;

    private Instant expiredAt;

    private TokenType tokenType;

    private final Map<String, Object> headers;
    private final Map<String, Object> claims;

    public DefaultLightningJwt(String tokenValue,
                               TokenType tokenType,
                               Instant issuedAt, Instant expiresAt, Map<String, Object> headers, Map<String, Object> claims) {
        Assert.notEmpty(headers, "headers cannot be empty");
        Assert.notEmpty(claims, "claims cannot be empty");
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<String, Object>(headers));
        this.claims = Collections.unmodifiableMap(new LinkedHashMap<String, Object>(claims));
        this.tokenValue = tokenValue;
        this.tokenType = tokenType;
        this.issuedAt = issuedAt;
        this.expiredAt = expiresAt;
    }

    public Map<String, Object> getHeaders() {
        return this.headers;
    }

    public Map<String, Object> getClaims() {
        return this.claims;
    }


    @Override
    public String getTokenValue() {
        return tokenValue;
    }

    @Override
    public TokenType getTokenType() {
        return tokenType;
    }


    public static DefaultLightningJwt.Builder withTokenValue(String tokenValue) {
        return new DefaultLightningJwt.Builder(tokenValue);
    }

    public static final class Builder {
        private String tokenValue;
        private TokenType tokenType;
        private final Map<String, Object> claims;
        private final Map<String, Object> headers;

        private Builder(String tokenValue) {
            this.claims = new LinkedHashMap<>();
            this.headers = new LinkedHashMap<>();
            this.tokenValue = tokenValue;
        }

        public DefaultLightningJwt.Builder tokenValue(String tokenValue) {
            this.tokenValue = tokenValue;
            return this;
        }

        public DefaultLightningJwt.Builder tokenType(TokenType tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public DefaultLightningJwt.Builder claim(String name, Object value) {
            this.claims.put(name, value);
            return this;
        }

        public DefaultLightningJwt.Builder claims(Consumer<Map<String, Object>> claimsConsumer) {
            claimsConsumer.accept(this.claims);
            return this;
        }

        public DefaultLightningJwt.Builder header(String name, Object value) {
            this.headers.put(name, value);
            return this;
        }

        public DefaultLightningJwt.Builder headers(Consumer<Map<String, Object>> headersConsumer) {
            headersConsumer.accept(this.headers);
            return this;
        }

        public DefaultLightningJwt.Builder audience(Collection<String> audience) {
            return this.claim("aud", audience);
        }

        public DefaultLightningJwt.Builder expiresAt(Instant expiresAt) {
            this.claim("exp", expiresAt);
            return this;
        }

        public DefaultLightningJwt.Builder jti(String jti) {
            this.claim("jti", jti);
            return this;
        }

        public DefaultLightningJwt.Builder issuedAt(Instant issuedAt) {
            this.claim("iat", issuedAt);
            return this;
        }

        public DefaultLightningJwt.Builder issuer(String issuer) {
            this.claim("iss", issuer);
            return this;
        }

        public DefaultLightningJwt.Builder notBefore(Instant notBefore) {
            this.claim("nbf", notBefore);
            return this;
        }

        public DefaultLightningJwt.Builder subject(String subject) {
            this.claim("sub", subject);
            return this;
        }

        public DefaultLightningJwt build() {
            Instant iat = this.toInstant(this.claims.get("iat"));
            Instant exp = this.toInstant(this.claims.get("exp"));
            return new DefaultLightningJwt(this.tokenValue,
                    this.tokenType,
                    iat, exp, this.headers, this.claims);
        }

        private Instant toInstant(Object timestamp) {
            if (timestamp != null) {
                Assert.isInstanceOf(Instant.class, timestamp, "timestamps must be of type Instant");
            }

            return (Instant) timestamp;
        }
    }
}
