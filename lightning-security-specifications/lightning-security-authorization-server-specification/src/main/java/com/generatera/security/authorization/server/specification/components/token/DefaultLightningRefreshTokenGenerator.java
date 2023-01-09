package com.generatera.security.authorization.server.specification.components.token;

import com.generatera.security.authorization.server.specification.components.token.format.plain.DefaultPlainToken;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;

import java.time.Instant;
import java.util.Base64;

/**
 * @author FLJ
 * @date 2023/1/6
 * @time 14:59
 * @Description 默认进行刷新token的生成 ..
 */
public class DefaultLightningRefreshTokenGenerator implements LightningRefreshTokenGenerator {
    private final StringKeyGenerator refreshTokenGenerator = new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 96);
    @Override
    public LightningToken.LightningRefreshToken generate(LightningSecurityTokenContext context) {
        if (!LightningTokenType.LightningAuthenticationTokenType.REFRESH_TOKEN_TYPE.equals(context.getTokenType())) {
            return null;
        } else {
            Instant issuedAt = Instant.now();
            Instant expiresAt = issuedAt.plus(context.getTokenSettings().getRefreshTokenTimeToLive());
            return new LightningAuthenticationRefreshToken(new DefaultPlainToken(this.refreshTokenGenerator.generateKey(), issuedAt, expiresAt));
        }
    }
}
