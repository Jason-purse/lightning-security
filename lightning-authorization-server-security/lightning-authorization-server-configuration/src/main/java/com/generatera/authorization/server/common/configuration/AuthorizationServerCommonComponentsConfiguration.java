package com.generatera.authorization.server.common.configuration;

import com.generatera.authorization.server.common.configuration.token.*;
import com.generatera.authorization.server.common.configuration.util.jose.Jwks;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;

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
    @ConditionalOnMissingBean(LightningAuthenticationTokenParser.class)
    public LightningAuthenticationTokenParser jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return new DefaultLightningAuthenticationTokenParser(jwkSource);
    }


    /**
     * token 生成器
     *
     *
     * @param properties component properties
     * @param jwkSource  jwtSource
     * @return tokenGenerator ..
     * <p>
     * 用户有机会提供自己的认证Token 生成器  ..
     * 包括 oauth2 / 或者 form-login 配置 ..
     *
     * 如果最后没有提供,则提供默认的 ..
     */
    @Bean(TOKEN_GENERATOR_NAME)
    @ConditionalOnMissingBean(LightningAuthenticationTokenGenerator.class)
    public LightningAuthenticationTokenGenerator lightningAuthenticationTokenGenerator(
            AuthorizationServerComponentProperties properties,
            JWKSource<SecurityContext> jwkSource) {
        Boolean isPlain = properties.getTokenSettings().getIsPlain();
        return new DefaultAuthenticationTokenGenerator(isPlain, jwkSource);
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
     * 当前我们自己第三方 授权服务提供商的一些端点配置 ..
     *
     * @return provider config
     */
    @Bean
    public ProviderSettings authorizationServerSettings(AuthorizationServerComponentProperties properties) {
        AuthorizationServerComponentProperties.ProviderSettingProperties settingProperties = properties.getProviderSettingProperties();

        final ProviderSettings.Builder builder = ProviderSettings
                .builder();

        // issuer 可以自动生成
        if (StringUtils.isNotBlank(settingProperties.getIssuer())) {
            builder.issuer(settingProperties.getIssuer());
        }
        return builder
                .authorizationEndpoint(settingProperties.getAuthorizationEndpoint())
                .tokenEndpoint(settingProperties.getTokenEndpoint())
                .jwkSetEndpoint(settingProperties.getJwkSetEndpoint())
                .tokenRevocationEndpoint(settingProperties.getTokenRevocationEndpoint())
                .tokenIntrospectionEndpoint(settingProperties.getTokenIntrospectionEndpoint())
                .oidcClientRegistrationEndpoint(settingProperties.getOidcClientRegistrationEndpoint())
                .oidcUserInfoEndpoint(settingProperties.getOidcUserInfoEndpoint())
                .build();
    }
}
