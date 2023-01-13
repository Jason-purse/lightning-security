package com.generatera.authorization.application.server.config;

import com.generatera.authorization.server.common.configuration.provider.metadata.oidc.OidcProviderConfigurationEndpointFilter;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

import static com.generatera.authorization.application.server.config.ApplicationAuthServerProperties.APPLICATION_AUTH_SERVER_PREFIX;

/**
 * 应用级别的 认证属性
 */
@Data
@ConfigurationProperties(prefix = APPLICATION_AUTH_SERVER_PREFIX)
public class ApplicationAuthServerProperties {

    public static final String APPLICATION_AUTH_SERVER_PREFIX = "lightning.auth.app.server.config";

    private final Permission permission = new Permission();

    private ServerMetaDataEndpointConfig serverMetaDataEndpointConfig = new ServerMetaDataEndpointConfig();


    @Data
    public static class ServerMetaDataEndpointConfig {

        public static final String ENABLE_OIDC = APPLICATION_AUTH_SERVER_PREFIX + ".serverMetaDataEndpointConfig.enableOidc";

        public static final String OPEN_CONNECT_ID_METADATA_ENDPOINT = OidcProviderConfigurationEndpointFilter.DEFAULT_OIDC_PROVIDER_CONFIGURATION_ENDPOINT_URI;

        /**
         * 那么默认需要 ..
         */
        private Boolean enableOidc = true;


    }


    @Data
    public static class Permission {

        /**
         * url 白名单 - 放行,不需要token 校验
         */
        private List<String> urlWhiteList = Collections.emptyList();
    }

}
