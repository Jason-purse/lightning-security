package com.generatera.security.application.authorization.server.token.specification;

import com.generatera.security.server.token.specification.LightningAuthorizationServerTokenSecurityContext;
import com.generatera.security.server.token.specification.LightningTokenType;
import com.generatera.security.server.token.specification.format.LightningTokenFormat;
import com.generatera.security.server.token.specification.format.jwt.DefaultLightningJwtGenerator;
import com.generatera.security.server.token.specification.generators.DefaultLightningAccessTokenGenerator;
import com.generatera.security.server.token.specification.generators.DefaultLightningRefreshTokenGenerator;
import com.generatera.security.server.token.specification.generators.LightningAccessTokenGenerator;
import com.generatera.security.server.token.specification.generators.LightningRefreshTokenGenerator;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.util.Assert;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 9:40
 * @Description 用户中心的 token 生成器 ..
 * <p>
 * 这种情况下,我们有两种选择 ...
 * 1. 简单生成 自省token
 * 2. 将用户信息放入token ..
 * <p>
 * 这里借助了jwt encoder 完成jwt 令牌的生成 ..
 * <p>
 * 默认实现  基于 jwt 实现 bearer token 的生成 ...
 */
public class DefaultApplicationLevelAuthenticationTokenGenerator implements LightningApplicationLevelAuthenticationTokenGenerator {
    /**
     * 代表是否自省Token
     */
    private Boolean isPlain = Boolean.FALSE;


    private LightningAccessTokenGenerator accessTokenGenerator;

    private LightningRefreshTokenGenerator refreshTokenGenerator;

    private LightningTokenFormat tokenFormat = LightningTokenFormat.JWT;


    public DefaultApplicationLevelAuthenticationTokenGenerator(Boolean isPlain, JWKSource<SecurityContext> jwkSource) {
        this.isPlain = isPlain;
        this.refreshTokenGenerator = new DefaultLightningRefreshTokenGenerator(
                new DefaultLightningJwtGenerator(jwkSource)
        );

        this.accessTokenGenerator = new DefaultLightningAccessTokenGenerator(
                new DefaultLightningJwtGenerator(jwkSource)
        );
    }

    public DefaultApplicationLevelAuthenticationTokenGenerator(JWKSource<SecurityContext> jwkSource) {
        this(Boolean.FALSE, jwkSource);
    }

    public void setAccessTokenGenerator(LightningAccessTokenGenerator accessTokenGenerator) {
        Assert.notNull(accessTokenGenerator, "accessTokenGenerator must not be null !!!");
        this.accessTokenGenerator = accessTokenGenerator;
    }

    public void setRefreshTokenGenerator(LightningRefreshTokenGenerator refreshTokenGenerator) {
        Assert.notNull(accessTokenGenerator, "refreshTokenGenerator must not be null !!!");
        this.refreshTokenGenerator = refreshTokenGenerator;
    }

    public void setIsPlain(Boolean isPlain) {
        Assert.notNull(isPlain, "isPlain must not be null !!!");
        this.isPlain = isPlain;
    }

    public void setTokenFormat(LightningTokenFormat tokenFormat) {
        Assert.notNull(tokenFormat, "token format must not be null !!!");
        this.tokenFormat = tokenFormat;
    }

    @Override
    public LightningApplicationLevelAuthenticationToken generate(LightningApplicationLevelAuthenticationSecurityContext securityContext) {
        return LightningApplicationLevelAuthenticationToken.of(
                accessTokenGenerator.generate(
                        LightningAuthorizationServerTokenSecurityContext.of(
                                LightningTokenType.LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE,
                                LightningTokenFormat.JWT,
                                LightningTokenType.LightningTokenValueType.BEARER_TOKEN_TYPE,
                                securityContext.getAuthentication(),
                                securityContext.getProviderContext(),
                                securityContext.getTokenSettings()
                        )
                ),
                refreshTokenGenerator.generate(
                        LightningAuthorizationServerTokenSecurityContext.of(
                                LightningTokenType.LightningAuthenticationTokenType.REFRESH_TOKEN_TYPE,
                                tokenFormat,
                                LightningTokenType.LightningTokenValueType.BEARER_TOKEN_TYPE,
                                securityContext.getAuthentication(),
                                securityContext.getProviderContext(),
                                securityContext.getTokenSettings()
                        )
                )
        );
    }
}
