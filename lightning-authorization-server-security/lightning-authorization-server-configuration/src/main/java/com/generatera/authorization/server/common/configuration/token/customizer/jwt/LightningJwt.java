package com.generatera.authorization.server.common.configuration.token.customizer.jwt;

import com.generatera.authorization.server.common.configuration.token.LightningToken;

import java.time.Instant;
import java.util.Map;
/**
 * @author FLJ
 * @date 2023/1/3
 * @time 10:35
 * @Description Lightning Jwt
 */
public interface LightningJwt extends JwtClaimAccessor, LightningToken {

    Map<String, Object> getHeaders();

    Map<String, Object> getClaims();


    @Override
    default Instant getExpiresAt() {
        return JwtClaimAccessor.super.getExpiresAt();
    }

    @Override
    default Instant getIssuedAt() {
        return JwtClaimAccessor.super.getIssuedAt();
    }
}
