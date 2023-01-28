package com.generatera.authorization.application.server.config.token;

import com.generatera.security.authorization.server.specification.components.token.format.JwtExtClaimNames;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.ClaimAccessor;
import org.springframework.lang.Nullable;

import java.net.URL;
import java.time.Instant;
import java.util.List;

public interface AuthTokenIntrospectionClaimAccessor extends ClaimAccessor {

    default boolean isActive() {
        return Boolean.TRUE.equals(this.getClaimAsBoolean("active"));
    }

    @Nullable
    default String getUsername() {
        return this.getClaimAsString("username");
    }

    @Nullable
    default List<String> getScopes() {
        return this.getClaimAsStringList(JwtExtClaimNames.SCOPE_CLAIM);
    }

    @Nullable
    default List<String> getAuthorities() {
        return this.getClaimAsStringList(JwtExtClaimNames.AUTHORITIES_CLAIM);
    }


    @Nullable
    default String getTokenType() {
        return this.getClaimAsString("token_type");
    }

    @Nullable
    default Instant getExpiresAt() {
        return this.getClaimAsInstant("exp");
    }

    @Nullable
    default Instant getIssuedAt() {
        return this.getClaimAsInstant("iat");
    }

    @Nullable
    default Instant getNotBefore() {
        return this.getClaimAsInstant("nbf");
    }

    @Nullable
    default String getSubject() {
        return this.getClaimAsString("sub");
    }

    @Nullable
    default List<String> getAudience() {
        return this.getClaimAsStringList("aud");
    }

    @Nullable
    default URL getIssuer() {
        return this.getClaimAsURL("iss");
    }

    @Nullable
    default String getId() {
        return this.getClaimAsString("jti");
    }
}