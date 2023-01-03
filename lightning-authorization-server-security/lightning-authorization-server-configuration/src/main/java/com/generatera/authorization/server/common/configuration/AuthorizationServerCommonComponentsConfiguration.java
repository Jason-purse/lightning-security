package com.generatera.authorization.server.common.configuration;

import com.generatera.authorization.server.common.configuration.token.DefaultAuthenticationTokenGenerator;
import com.generatera.authorization.server.common.configuration.token.LightningAuthenticationTokenGenerator;
import com.generatera.authorization.server.common.configuration.token.TokenSettings;
import com.generatera.authorization.server.common.configuration.token.TokenSettingsProvider;
import com.generatera.authorization.server.common.configuration.util.jose.Jwks;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Duration;

import static com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties.TOKEN_GENERATOR_NAME;

/**
 * 授权服务器的 通用组件配置
 */
@Configuration
@AutoConfigureBefore(OAuth2AuthorizationServerConfiguration.class)
@EnableConfigurationProperties(AuthorizationServerComponentProperties.class)
@Import(AuthorizationServerComponentImportSelector.class)
public class AuthorizationServerCommonComponentsConfiguration {

    /**
     * jwk set
     */
    @Bean
    @ConditionalOnMissingBean(JWKSource.class)
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    /**
     * 解码器 ... 必要 ...
     * @param jwkSource jwtSource ....
     * @return
     */

    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * 需要配置SettingProvider
     *
     * ProviderContextHolder 需要单独处理
     */
    @Bean
    public TokenSettingsProvider settingsProvider(AuthorizationServerComponentProperties properties) {

        TokenSettings.Builder builder = TokenSettings.builder();

        return new TokenSettingsProvider(
                builder
                        .accessTokenFormat(properties.getTokenSettings().getTokenFormat())
                        .accessTokenTimeToLive(Duration.ofMillis(properties.getTokenSettings().getAccessTokenTimeToLive()))
                        .refreshTokenTimeToLive(Duration.ofMillis(properties.getTokenSettings().getRefreshTokenTimeToLive()))
                        .reuseRefreshTokens(properties.getTokenSettings().getReuseRefreshToken())
                        .build()
        );
    }



    /**
     * token 生成器
     *
     * @param properties component properties
     * @param jwkSource  jwtSource
     * @return tokenGenerator ..
     * <p>
     * 用户有机会提供自己的认证Token 生成器  ..
     * 包括 oauth2 / 或者 form-login 配置 ..
     */
    @Bean(TOKEN_GENERATOR_NAME)
    @ConditionalOnMissingBean(LightningAuthenticationTokenGenerator.class)
    public LightningAuthenticationTokenGenerator lightningAuthenticationTokenGenerator(
            AuthorizationServerComponentProperties properties,
            JWKSource<SecurityContext> jwkSource) {
        Boolean isPlain = properties.getTokenSettings().getIsPlain();
        return new DefaultAuthenticationTokenGenerator(isPlain != null ? isPlain : Boolean.FALSE, jwkSource);
    }
}
