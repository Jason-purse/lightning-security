package com.generatera.security.authorization.server.specification.components.token;

import com.generatera.security.authorization.server.specification.components.token.format.jwt.ClaimAccessor;

import java.net.URL;
import java.time.Instant;
import java.util.List;

public interface LightningTokenClaimAccessor extends ClaimAccessor {
    default URL getIssuer() {
        return this.getClaimAsURL("iss");
    }

    default String getSubject() {
        return this.getClaimAsString("sub");
    }

    default List<String> getAudience() {
        return this.getClaimAsStringList("aud");
    }

    default Instant getExpiresAt() {
        return this.getClaimAsInstant("exp");
    }

    default Instant getNotBefore() {
        return this.getClaimAsInstant("nbf");
    }

    default Instant getIssuedAt() {
        return this.getClaimAsInstant("iat");
    }

    default String getId() {
        return this.getClaimAsString("jti");
    }

}