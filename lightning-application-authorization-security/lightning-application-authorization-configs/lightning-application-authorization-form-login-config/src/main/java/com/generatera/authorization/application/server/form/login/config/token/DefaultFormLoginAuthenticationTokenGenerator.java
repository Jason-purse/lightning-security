package com.generatera.authorization.application.server.form.login.config.token;

import com.generatera.authorization.server.common.configuration.token.*;
import com.generatera.authorization.server.common.configuration.token.customizer.jwt.LightningJwt;
import com.generatera.authorization.server.common.configuration.token.customizer.jwt.jose.NimbusJwtEncoder;
import com.jianyue.lightning.boot.starter.util.SnowflakeIdWorker;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.util.Assert;

import java.time.Instant;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 9:40
 * @Description 表单登录情况下的 token 生成器 ..
 * <p>
 * 这种情况下,我们有两种选择 ...
 * 1. 简单生成 自省token
 * 2. 将用户信息放入token ..
 */
public class DefaultFormLoginAuthenticationTokenGenerator implements FormLoginAuthenticationTokenGenerator {


    public DefaultFormLoginAuthenticationTokenGenerator(JWKSource<SecurityContext> jwkSource) {
        this(Boolean.FALSE,jwkSource);
    }

    public DefaultFormLoginAuthenticationTokenGenerator(Boolean isPlain, JWKSource<SecurityContext> jwkSource) {
        this.isPlain = isPlain;
        this.jwkSource = jwkSource;
        accessTokenGenerator = new LightningFormLoginAccessTokenGenerator() {

            private final SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker();

            private final FormLoginJwtGenerator jwtEncoder = new FormLoginJwtGenerator(
                    new NimbusJwtEncoder(jwkSource)
            );

            @Override
            public LightningToken.AccessToken generate(LightningSecurityContext context) {
                TokenSettings tokenSettings = context.getTokenSettings();
                if (isPlain) {
                    Instant now = Instant.now();
                    return LightningToken.accessToken(
                            snowflakeIdWorker.nextId(),
                            now,
                            Instant.ofEpochMilli(now.toEpochMilli() + tokenSettings.getAccessTokenTimeToLive().toMillis())
                    );
                } else {
                    LightningJwt token = jwtEncoder.generate(((FormLoginSecurityContext) context));
                    Assert.notNull(token,"cannot generate jwt token !!!!");
                    String tokenValue = token.getTokenValue();
                    assert tokenValue != null;
                    return LightningToken.accessToken(tokenValue,
                            token.getIssuedAt(), token.getExpiresAt());
                }
            }
        };
        refreshTokenGenerator = new LightningFormLoginRefreshTokenGenerator() {

            private final SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker();


            @Override
            public LightningToken.RefreshToken generate(LightningSecurityContext context) {
                TokenSettings tokenSettings = context.getTokenSettings();
                Instant now = Instant.now();
                return LightningToken.refreshToken(
                        snowflakeIdWorker.nextId(),
                        now,
                        Instant.ofEpochMilli(now.toEpochMilli() + tokenSettings.getAccessTokenTimeToLive().toMillis())
                );
            }
        };
    }

    /**
     * 代表是否自省Token
     */
    private Boolean isPlain = Boolean.TRUE;

    private final JWKSource<SecurityContext> jwkSource;



    private LightningFormLoginAccessTokenGenerator accessTokenGenerator;

    private LightningFormLoginRefreshTokenGenerator refreshTokenGenerator;




    public void setAccessTokenGenerator(LightningFormLoginAccessTokenGenerator accessTokenGenerator) {
        Assert.notNull(accessTokenGenerator,"accessTokenGenerator must not be null !!!");
        this.accessTokenGenerator = accessTokenGenerator;
    }

    public void setRefreshTokenGenerator(LightningFormLoginRefreshTokenGenerator refreshTokenGenerator) {
        Assert.notNull(accessTokenGenerator,"refreshTokenGenerator must not be null !!!");
        this.refreshTokenGenerator = refreshTokenGenerator;
    }

    public void setIsPlain(Boolean isPlain) {
        Assert.notNull(isPlain,"isPlain must not be null !!!");
        this.isPlain  = isPlain;
    }

    @Override
    public LightningAuthenticationToken generate(LightningAuthenticationSecurityContext securityContext) {
        return LightningAuthenticationToken.of(
                accessTokenGenerator.generate(
                        FormLoginSecurityContext.of(
                                LightningToken.TokenType.ACCESS_TOKEN_TYPE,
                                securityContext.getAuthentication(),
                                securityContext.getProviderContext(),
                                securityContext.getTokenSettings()
                        )
                ),
                refreshTokenGenerator.generate(
                        FormLoginSecurityContext.of(
                                LightningToken.TokenType.REFRESH_TOKEN_TYPE,
                                securityContext.getAuthentication(),
                                securityContext.getProviderContext(),
                                securityContext.getTokenSettings()
                        )
                )
        );
    }
}
