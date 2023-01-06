package com.generatera.security.server.token.specification.generators;

import com.generatera.security.server.token.specification.LightningAuthorizationServerSecurityContext;
import com.generatera.security.server.token.specification.LightningAuthorizationServerTokenSecurityContext;
import com.generatera.security.server.token.specification.LightningTokenType.LightningTokenValueType;
import com.generatera.security.server.token.specification.format.LightningTokenFormat;
import com.generatera.security.server.token.specification.format.jwt.LightningAuthorizationServerJwtGenerator;
import com.generatera.security.server.token.specification.format.jwt.LightningJwt;
import com.generatera.security.server.token.specification.type.LightningAccessToken;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

/**
 * @author FLJ
 * @date 2023/1/6
 * @time 13:51
 * @Description  默认是基于 jwt 生成访问Token
 */
public class DefaultLightningAccessTokenGenerator implements LightningAccessTokenGenerator {

    private final LightningAuthorizationServerJwtGenerator jwtGenerator;

    public DefaultLightningAccessTokenGenerator(LightningAuthorizationServerJwtGenerator jwtGenerator) {
        Assert.notNull(jwtGenerator,"jwt generator cannot be null !!!");
        this.jwtGenerator = jwtGenerator;
    }


    @Override
    public LightningAccessToken generate(LightningAuthorizationServerSecurityContext context) {
        LightningAuthorizationServerTokenSecurityContext of = LightningAuthorizationServerTokenSecurityContext.of(
                context,
                LightningTokenFormat.JWT,
                getTokenValueType()
        );
        LightningJwt jwt = jwtGenerator.generate(of);
        return new LightningAccessToken(jwt);
    }

    @NotNull
    @Override
    public LightningTokenValueType getTokenValueType() {
        return LightningTokenValueType.BEARER_TOKEN_TYPE;
    }

}
