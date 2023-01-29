package com.generatera.authorization.application.server.config;

import com.generatera.authorization.server.common.configuration.provider.metadata.oidc.OidcProviderConfigurationEndpointFilter;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettingProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.generatera.authorization.application.server.config.ApplicationAuthServerProperties.APPLICATION_AUTH_SERVER_PREFIX;

/**
 * 应用级别的 认证属性
 */
@Data
@ConfigurationProperties(prefix = APPLICATION_AUTH_SERVER_PREFIX)
public class ApplicationAuthServerProperties {

    public static final String APPLICATION_AUTH_SERVER_PREFIX = "lightning.app.auth.server.config";

    private ServerMetaDataEndpointConfig serverMetaDataEndpointConfig = new ServerMetaDataEndpointConfig();
    /**
     * 是否前后端分离
     */
    private Boolean isSeparation = false;

    private final BackendSeparation backendSeparation = new BackendSeparation();


    /**
     * 提供者配置属性
     * 为元数据端点提供属性 为资源服务器 交换公钥 提供机会 ...
     */
    private final ProviderSettingProperties providerSettingProperties = new ProviderSettingProperties();


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
    public static class BackendSeparation {

        /**
         * 它主要控制,以下两个选项 ..
         */
        private Boolean enableAccountStatusDetails = false;

        private String accountExpiredMessage = "ACCOUNT EXPIRED";

        private String accountLockedMessage = "ACCOUNT FORBIDDEN";




        /**
         * 主要管理 账户异常信息(accountStatusMessage, 以及 badCredentialMessage),以及 enableAccountStatusDetails的控制能力 ..
         * 主要以下四项 ..
         */
        private Boolean enableAuthFailureDetails = true;

        private String accountStatusMessage = "ACCOUNT STATUS EXCEPTION";


        private String badCredentialMessage = "BAD CREDENTIAL ERROR";

        private String loginSuccessMessage = "LOGIN SUCCESS";

        private String loginFailureMessage = "LOGIN FAILURE";
    }
}
