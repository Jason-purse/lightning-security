package com.generatera.security.server.token.specification.generators;

import com.generatera.security.server.token.specification.LightningAuthorizationServerSecurityContext;
import com.generatera.security.server.token.specification.LightningAuthorizationServerTokenSecurityContext;
import com.generatera.security.server.token.specification.LightningToken;
import com.generatera.security.server.token.specification.LightningTokenType;
import com.generatera.security.server.token.specification.format.LightningTokenFormat;
import com.generatera.security.server.token.specification.format.jwt.LightningAuthorizationServerJwtGenerator;
import com.generatera.security.server.token.specification.format.jwt.LightningJwt;
import com.generatera.security.server.token.specification.type.LightningRefreshToken;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;
/**
 * @author FLJ
 * @date 2023/1/6
 * @time 14:59
 * @Description 默认基于 JWT 进行刷新token的生成 ..
 */
public class DefaultLightningRefreshTokenGenerator implements LightningRefreshTokenGenerator {

    private final LightningAuthorizationServerJwtGenerator jwtGenerator;

    public DefaultLightningRefreshTokenGenerator(LightningAuthorizationServerJwtGenerator jwtGenerator) {
        Assert.notNull(jwtGenerator, "jwt generator cannot be null !!!");
        this.jwtGenerator = jwtGenerator;
    }

    @Override
    public LightningToken.LightningRefreshToken generate(LightningAuthorizationServerSecurityContext context) {
        LightningAuthorizationServerTokenSecurityContext of = LightningAuthorizationServerTokenSecurityContext.of(
                context,
                LightningTokenFormat.JWT,
                getTokenValueType()
        );
        LightningJwt jwt = jwtGenerator.generate(of);
        return new LightningRefreshToken(jwt);
    }

    @NotNull
    @Override
    public LightningTokenType.LightningTokenValueType getTokenValueType() {
        return LightningTokenType.LightningTokenValueType.BEARER_TOKEN_TYPE;
    }
}
