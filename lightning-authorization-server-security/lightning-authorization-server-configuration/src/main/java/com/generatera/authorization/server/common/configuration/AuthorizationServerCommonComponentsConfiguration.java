package com.generatera.authorization.server.common.configuration;

import com.generatera.authorization.server.common.configuration.provider.ProviderSettings;
import com.generatera.authorization.server.common.configuration.provider.ProviderSettingsProvider;
import com.generatera.authorization.server.common.configuration.util.jose.Jwks;
import com.generatera.security.authorization.server.specification.TokenSettings;
import com.generatera.security.authorization.server.specification.TokenSettingsProvider;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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
                        .accessTokenIssueFormat(properties.getTokenSettings().getTokenFormat())
                        .accessTokenTimeToLive(Duration.ofMillis(properties.getTokenSettings().getAccessTokenTimeToLive()))
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
}
