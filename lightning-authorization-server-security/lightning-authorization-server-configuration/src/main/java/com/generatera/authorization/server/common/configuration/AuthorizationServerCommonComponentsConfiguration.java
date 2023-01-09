package com.generatera.authorization.server.common.configuration;

import com.generatera.authorization.server.common.configuration.authorization.LightningAuthorizationService;
import com.generatera.authorization.server.common.configuration.authorization.store.DefaultAuthenticationTokenService;
import com.generatera.authorization.server.common.configuration.authorization.store.LightningAuthenticationTokenService;
import com.generatera.security.authorization.server.specification.ProviderSettingsProvider;
import com.generatera.security.authorization.server.specification.TokenSettings;
import com.generatera.security.authorization.server.specification.TokenSettingsProvider;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettings;
import com.generatera.security.authorization.server.specification.components.token.*;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.DefaultLightningJwtGenerator;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JWKSourceProvider;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.LightningJwtEncoder;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.LightningJwtGenerator;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.jose.NimbusJwtEncoder;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 授权服务器的 通用组件配置
 * <p>
 * 希望非oauth2 / 或者 oauth2 都遵循 oauth2的一部分规范(token 解析)
 * 例如: 1. token 自解析
 * 2. token 撤销
 * 3. token 自省
 *
 *
 * 当前授权服务器遵循的规范是,都有统一的jwk set
 * 都有统一的 providerSettings
 * 都有统一的 token settings...
 *
 * 但是对于 oauth2 来说(token settings 以 client registration 注册信息为准,如果没有才考虑这个同一个的token settings配置)
 */
@Configuration
@AutoConfigureBefore(SecurityAutoConfiguration.class)
@EnableConfigurationProperties(AuthorizationServerComponentProperties.class)
public class AuthorizationServerCommonComponentsConfiguration {

    /**
     * jwk set
     */
    @Bean
    @ConditionalOnMissingBean(JWKSource.class)
    public JWKSourceProvider jwkSource() {
        return new JWKSourceProvider();
    }


    /**
     * 需要 authorization service
     */
    @Bean
    @ConditionalOnMissingBean(LightningAuthorizationService.class)
    public LightningAuthenticationTokenService authorizationService() {
        return new DefaultAuthenticationTokenService();
    }


    /**
     * 需要配置SettingProvider
     * <p>
     * ProviderContextHolder 需要单独处理
     */
    @Bean
    public TokenSettingsProvider settingsProvider(AuthorizationServerComponentProperties properties) {

        TokenSettings.Builder builder = TokenSettings.builder();

        return new TokenSettingsProvider(
                builder
                        .audience(properties.getTokenSettings().getAudiences())
                        .accessTokenIssueFormat(properties.getTokenSettings().getTokenIssueFormat())
                        .accessTokenValueType(properties.getTokenSettings().getTokenValueType())
                        .accessTokenTimeToLive(Duration.ofMillis(properties.getTokenSettings().getAccessTokenTimeToLive()))
                        .refreshTokenIssueFormat(properties.getTokenSettings().getTokenIssueFormat())
                        .refreshTokenValueType(properties.getTokenSettings().getTokenValueType())
                        .refreshTokenTimeToLive(Duration.ofMillis(properties.getTokenSettings().getRefreshTokenTimeToLive()))
                        .reuseRefreshTokens(properties.getTokenSettings().getReuseRefreshToken())
                        .build()
        );
    }


    /**
     * 这样做,是为了 统一的 token 生成策略 ...
     *
     * 例如表单也可以遵循 oauth2 的部分规则进行 jwk url 地址查找,从而进一步配置自身 。。
     * @param properties properties ..
     * @return ProviderSettingsProvider
     */
    @Bean
    public ProviderSettingsProvider provider(AuthorizationServerComponentProperties properties) {
        AuthorizationServerComponentProperties.ProviderSettingProperties settingProperties = properties.getProviderSettingProperties();
        final ProviderSettings.Builder builder = ProviderSettings
                .builder();

        // issuer 可以自动生成
        if (StringUtils.isNotBlank(settingProperties.getIssuer())) {
            builder.issuer(settingProperties.getIssuer());
        }
        ProviderSettings settings = builder
                .authorizationEndpoint(settingProperties.getAuthorizationEndpoint())
                .tokenEndpoint(settingProperties.getTokenEndpoint())
                .jwkSetEndpoint(settingProperties.getJwkSetEndpoint())
                .tokenRevocationEndpoint(settingProperties.getTokenRevocationEndpoint())
                .tokenIntrospectionEndpoint(settingProperties.getTokenIntrospectionEndpoint())
                .oidcClientRegistrationEndpoint(settingProperties.getOidcClientRegistrationEndpoint())
                .oidcUserInfoEndpoint(settingProperties.getOidcUserInfoEndpoint())
                .build();

        return new ProviderSettingsProvider(settings);
    }



    // --------------------------- token 生成器 ---------------------------------------
    @Bean
    @ConditionalOnMissingBean({LightningTokenGenerator.class, LightningJwtEncoder.class})
    public LightningTokenGenerator<LightningToken> tokenGenerator(JWKSourceProvider jwkSourceProvider
    ) {
        return new DelegatingLightningTokenGenerator(
                new DefaultLightningAccessTokenGenerator(),
                new DefaultLightningRefreshTokenGenerator(),
                new DefaultLightningJwtGenerator(new NimbusJwtEncoder(jwkSourceProvider.getJWKSource()))
        );
    }

    @Bean
    @ConditionalOnMissingBean(LightningTokenGenerator.class)
    @ConditionalOnBean(LightningJwtEncoder.class)
    public LightningTokenGenerator<LightningToken> tokenGenerator(LightningJwtEncoder jwtEncoder) {
        return new DelegatingLightningTokenGenerator(
                new DefaultLightningAccessTokenGenerator(),
                new DefaultLightningRefreshTokenGenerator(),
                new DefaultLightningJwtGenerator(jwtEncoder)
        );
    }

    // --------------------------------------------------------------------------------


}
