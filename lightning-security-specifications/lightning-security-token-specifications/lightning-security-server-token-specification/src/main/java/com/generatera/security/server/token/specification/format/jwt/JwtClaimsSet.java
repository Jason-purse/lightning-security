package com.generatera.security.server.token.specification.format.jwt;

import com.generatera.security.server.token.specification.format.jwt.converter.ClaimConversionService;
import org.springframework.util.Assert;

import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class JwtClaimsSet implements JwtClaimAccessor {
    private final Map<String, Object> claims;

    private JwtClaimsSet(Map<String, Object> claims) {
        this.claims = Map.copyOf(claims);
    }

    public Map<String, Object> getClaims() {
        return this.claims;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder from(JwtClaimsSet claims) {
        return new Builder(claims);
    }

    public static Builder from(Map<String,Object> claims) {
        return new Builder(claims);
    }

    public static final class Builder {
        private final Map<String, Object> claims;

        private Builder() {
            this.claims = new HashMap<>();
        }

        private Builder(JwtClaimsSet claims) {
            this.claims = new HashMap<>();
            Assert.notNull(claims, "claims cannot be null");
            this.claims.putAll(claims.getClaims());
        }

        private Builder(Map<String,Object> claims) {
            this.claims = new HashMap<>();
            Assert.notNull(claims, "claims cannot be null");
            this.claims.putAll(claims);
        }

        public Builder issuer(String issuer) {
            return this.claim("iss", issuer);
        }

        public Builder subject(String subject) {
            return this.claim("sub", subject);
        }

        public Builder audience(List<String> audience) {
            return this.claim("aud", audience);
        }

        public Builder expiresAt(Instant expiresAt) {
            return this.claim("exp", expiresAt);
        }

        public Builder notBefore(Instant notBefore) {
            return this.claim("nbf", notBefore);
        }

        public Builder issuedAt(Instant issuedAt) {
            return this.claim("iat", issuedAt);
        }

        public Builder id(String jti) {
            return this.claim("jti", jti);
        }

        public Builder claim(String name, Object value) {
            Assert.hasText(name, "name cannot be empty");
            Assert.notNull(value, "value cannot be null");
            this.claims.put(name, value);
            return this;
        }

        public Builder claims(Consumer<Map<String, Object>> claimsConsumer) {
            claimsConsumer.accept(this.claims);
            return this;
        }

        public JwtClaimsSet build() {
            Assert.notEmpty(this.claims, "claims cannot be empty");
            Object issuer = this.claims.get("iss");
            if (issuer != null) {
                URL convertedValue = (URL) ClaimConversionService.getSharedInstance().convert(issuer, URL.class);
                if (convertedValue != null) {
                    this.claims.put("iss", convertedValue);
                }
            }

            return new JwtClaimsSet(this.claims);
        }
    }
}
