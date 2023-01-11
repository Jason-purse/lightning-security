package com.generatera.authorization.application.server.config;

import com.generatera.authorization.server.common.configuration.provider.metadata.oidc.OidcProviderConfigurationEndpointFilter;
import com.generatera.security.authorization.server.specification.ProviderSettingsProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author FLJ
 * @date 2023/1/11
 * @time 15:06
 * @Description auth server provider 元数据处理 ...
 */
@AutoConfiguration
public class AuthServerProviderMetadataConfiguration {

    /**
     * 处理 oidc provider server meta data 请求控制器处理 ...,
     * 如果不使用这个,则添加一个 ProviderFilter ...
     */
    @RestController
    @RequestMapping(ApplicationAuthServerProperties.ServerMetaDataEndpointConfig.OPEN_CONNECT_ID_METADATA_ENDPOINT)
    public static class NoOidcProviderServerMetadataController {

        /**
         * 这里必须是 404
         */
        @GetMapping
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public void noOidcProviderConfig() {
        }
    }

    public static class OidcProviderServerMetadataEnabler {

        @Bean
        public LightningAppAuthServerConfigurer appAuthServerConfigurer(ProviderSettingsProvider provider) {
            return new LightningAppAuthServerConfigurer() {
                @Override
                public void configure(HttpSecurity securityBuilder) throws Exception {
                    securityBuilder.addFilterBefore(
                            new OidcProviderConfigurationEndpointFilter(provider.getProviderSettings()),
                            AbstractPreAuthenticatedProcessingFilter.class);
                }
            };
        }
    }

}
