package com.generatera.central.oauth2.authorization.server.configuration;


import com.generatera.authorization.application.server.config.ApplicationAuthServerConfig;
import com.generatera.authorization.application.server.config.LightningAppAuthServerConfigurer;
import com.generatera.authorization.application.server.form.login.config.ApplicationFormLoginConfiguration;
import com.generatera.authorization.application.server.form.login.config.FormLoginProperties;
import com.generatera.security.authorization.server.specification.ProviderSettingsProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * oauth2 central authorization server com.generatera.oauth2.resource.server.config
 *
 * 1. oauth2 central authorization server的 一部分组件注册
 *      1.1 clientRegistrationRepository
 */
@Slf4j
@Configuration
@AutoConfiguration
@AutoConfigureBefore({ApplicationAuthServerConfig.class,ApplicationFormLoginConfiguration.class})
@EnableConfigurationProperties({OAuth2CentralAuthorizationServerProperties.class, FormLoginProperties.class})
@Import(OAuth2CentralAuthorizationServerCCImportSelector.class)
public class OAuth2CentralAuthorizationServerConfiguration {

    @Bean
    public ProviderSettings providerSettings(ProviderSettingsProvider provider) {
        com.generatera.security.authorization.server.specification.components.provider.ProviderSettings
                providerSettings = provider.getProviderSettings();

        // 转交给 oauth2 ..
        return ProviderSettings.withSettings(providerSettings.getSettings()).build();
    }



    @Bean
    @SuppressWarnings("unchecked")
    public LightningAppAuthServerConfigurer configurer(
            @Autowired(required = false)
            List<LightningOAuth2CentralAuthorizationServerExtConfigurer> extConfigurers
    ) {
        return new LightningAppAuthServerConfigurer() {
            @Override
            public void configure(HttpSecurity securityBuilder) throws Exception {

                OAuth2AuthorizationServerConfigurer<HttpSecurity> configurer = securityBuilder.getConfigurer(OAuth2AuthorizationServerConfigurer.class);
                if(configurer == null) {
                    configurer
                            = OAuth2AuthorizationServerConfigurerExtUtils.getOAuth2AuthorizationServerConfigurer(securityBuilder);
                    securityBuilder.apply(configurer);
                }

                // 增加扩展
                if(!CollectionUtils.isEmpty(extConfigurers)) {
                    for (LightningOAuth2CentralAuthorizationServerExtConfigurer extConfigurer : extConfigurers) {
                        extConfigurer.configure(configurer);
                    }
                }
            }
        };
    }
}
