package com.generatera.authorization.server.common.configuration.provider;

import com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;

@Configuration
public class AuthorizationServerProviderComponentConfiguration {

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
