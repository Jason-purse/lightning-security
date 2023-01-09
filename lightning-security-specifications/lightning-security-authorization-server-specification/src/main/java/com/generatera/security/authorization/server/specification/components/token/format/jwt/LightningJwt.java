package com.generatera.security.authorization.server.specification.components.token.format.jwt;

import com.generatera.security.authorization.server.specification.components.token.LightningToken.ComplexToken;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningTokenValueType;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
/**
 * @author FLJ
 * @date 2023/1/6
 * @time 12:55
 * @Description 主要是用来生成JWT的 ..
 */
public class LightningJwt extends ComplexToken implements JwtClaimAccessor {
    private final Map<String, Object> headers;
    private final Map<String, Object> claims;

    public LightningJwt(String tokenValue, Instant issuedAt, Instant expiresAt, Map<String, Object> headers, Map<String, Object> claims) {
        this(LightningTokenValueType.BEARER_TOKEN_TYPE,tokenValue, issuedAt, expiresAt,headers,claims);
    }

    public LightningJwt(LightningTokenValueType tokenValueType, String tokenValue, Instant issuedAt, Instant expiresAt, Map<String, Object> headers, Map<String, Object> claims) {
        super(tokenValueType, tokenValue,issuedAt, expiresAt);
        Assert.notEmpty(headers, "headers cannot be empty");
        Assert.notEmpty(claims, "claims cannot be empty");
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        this.claims = Collections.unmodifiableMap(new LinkedHashMap<>(claims));
    }


    public Map<String, Object> getHeaders() {
        return this.headers;
    }

    public Map<String, Object> getClaims() {
        return this.claims;
    }

    public static LightningJwt.Builder withTokenValue(String tokenValue) {
        return new LightningJwt.Builder(tokenValue);
    }

    public static final class Builder {
        private String tokenValue;
        private final Map<String, Object> claims;
        private final Map<String, Object> headers;

        private Builder(String tokenValue) {
            this.claims = new LinkedHashMap<>();
            this.headers = new LinkedHashMap<>();
            this.tokenValue = tokenValue;
        }

        public LightningJwt.Builder tokenValue(String tokenValue) {
            this.tokenValue = tokenValue;
            return this;
        }

        public LightningJwt.Builder claim(String name, Object value) {
            this.claims.put(name, value);
            return this;
        }

        public LightningJwt.Builder claims(Consumer<Map<String, Object>> claimsConsumer) {
            claimsConsumer.accept(this.claims);
            return this;
        }

        public LightningJwt.Builder header(String name, Object value) {
            this.headers.put(name, value);
            return this;
        }

        public LightningJwt.Builder headers(Consumer<Map<String, Object>> headersConsumer) {
            headersConsumer.accept(this.headers);
            return this;
        }

        public LightningJwt.Builder audience(Collection<String> audience) {
            return this.claim("aud", audience);
        }

        public LightningJwt.Builder expiresAt(Instant expiresAt) {
            this.claim("exp", expiresAt);
            return this;
        }

        public LightningJwt.Builder jti(String jti) {
            this.claim("jti", jti);
            return this;
        }

        public LightningJwt.Builder issuedAt(Instant issuedAt) {
            this.claim("iat", issuedAt);
            return this;
        }

        public LightningJwt.Builder issuer(String issuer) {
            this.claim("iss", issuer);
            return this;
        }

        public LightningJwt.Builder notBefore(Instant notBefore) {
            this.claim("nbf", notBefore);
            return this;
        }

        public LightningJwt.Builder subject(String subject) {
            this.claim("sub", subject);
            return this;
        }

        public LightningJwt build() {
            Instant iat = this.toInstant(this.claims.get("iat"));
            Instant exp = this.toInstant(this.claims.get("exp"));
            return new LightningJwt(this.tokenValue, iat, exp, this.headers, this.claims);
        }

        private Instant toInstant(Object timestamp) {
            if (timestamp != null) {
                Assert.isInstanceOf(Instant.class, timestamp, "timestamps must be of type Instant");
            }

            return (Instant)timestamp;
        }
    }
}