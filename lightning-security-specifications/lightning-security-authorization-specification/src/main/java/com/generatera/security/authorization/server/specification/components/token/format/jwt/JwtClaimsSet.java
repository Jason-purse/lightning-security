package com.generatera.security.authorization.server.specification.components.token.format.jwt;

import com.generatera.security.authorization.server.specification.components.token.format.jwt.converter.ClaimConversionService;
import org.springframework.util.Assert;

import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author FLJ
 * @date 2023/1/17
 * @time 10:28
 * @Description 表示jwt claims 集合 ..
 */
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

    public static Builder from(Map<String, Object> claims) {
        return new Builder(claims);
    }

    public static final class Builder implements JwtClaimAccessor {
        private final Map<String, Object> claims;

        private Builder() {
            this.claims = new HashMap<>();
        }

        private Builder(JwtClaimsSet claims) {
            this.claims = new HashMap<>();
            Assert.notNull(claims, "claims cannot be null");
            this.claims.putAll(claims.getClaims());
        }

        private Builder(Map<String, Object> claims) {
            this.claims = new HashMap<>();
            Assert.notNull(claims, "claims cannot be null");
            this.claims.putAll(claims);
        }

        public Builder issuer(String issuer) {
            return claim("iss", issuer);
        }

        public Builder subject(String subject) {
            return claim("sub", subject);
        }

        public Builder audience(List<String> audience) {
            return claim("aud", audience);
        }

        public Builder expiresAt(Instant expiresAt) {
            return claim("exp", expiresAt);
        }

        public Builder notBefore(Instant notBefore) {
            return claim("nbf", notBefore);
        }

        public Builder issuedAt(Instant issuedAt) {
            return claim("iat", issuedAt);
        }

        public Builder id(String jti) {
            return claim("jti", jti);
        }


        public JwtClaimsSet build() {
            Assert.notEmpty(this.claims, "claims cannot be empty");
            Object issuer = this.claims.get("iss");
            if (issuer != null) {
                URL convertedValue = ClaimConversionService.getSharedInstance().convert(issuer, URL.class);
                if (convertedValue != null) {
                    this.claims.put("iss", convertedValue);
                }
            }

            return new JwtClaimsSet(this.claims);
        }

        @Override
        public Map<String, Object> getClaims() {
            return claims;
        }
    }
}
