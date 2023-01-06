package com.generatera.security.server.token.specification.format.jwt;

import java.net.URL;
import java.time.Instant;
import java.util.List;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 10:01
 * @Description spring oauth2 jwt ClaimAccessor copy ..
 */
public interface JwtClaimAccessor extends ClaimAccessor {

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