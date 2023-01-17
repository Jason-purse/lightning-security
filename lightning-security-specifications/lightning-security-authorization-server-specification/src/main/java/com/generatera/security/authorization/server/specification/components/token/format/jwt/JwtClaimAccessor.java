package com.generatera.security.authorization.server.specification.components.token.format.jwt;

import org.springframework.util.Assert;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 10:01
 * @Description spring oauth2 jwt ClaimAccessor copy ..
 *
 * 常用claims 获取快捷方式 ..
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


    default <T extends JwtClaimAccessor> T claim(String name, Object value) {
        Assert.hasText(name, "name cannot be empty");
        Assert.notNull(value, "value cannot be null");
        this.getClaims().put(name, value);
        return (T)this;
    }

    default <T extends JwtClaimAccessor> T claims(Consumer<Map<String, Object>> claimsConsumer) {
        claimsConsumer.accept(this.getClaims());
        return (T)this;
    }

    default <T extends JwtClaimAccessor> T removeClaim(String name) {
        Assert.hasText(name, "name cannot be empty");
        this.getClaims().remove(name);
        return (T)this;
    }

}