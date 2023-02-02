package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.application.server.config.util.AppAuthConfigurerUtils;
import com.generatera.authorization.application.server.config.util.ApplicationAuthServerUtils;
import com.generatera.authorization.server.common.configuration.LightningCentralAuthServer;
import com.generatera.authorization.server.common.configuration.provider.metadata.oidc.OidcProviderConfigurationEndpointFilter;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class OpenConnectAuthServerMetadataConfigurer extends AbstractAuthConfigurer {

    private RequestMatcher requestMatcher;

    public OpenConnectAuthServerMetadataConfigurer(ObjectPostProcessor<Object> objectPostProcessor) {
        super(objectPostProcessor);
    }

    @Override
    public <B extends HttpSecurityBuilder<B>> void init(B builder) {
        ApplicationAuthServerUtils applicationAuthServerUtils = ApplicationAuthServerUtils.getApplicationAuthServerProperties(builder);
        // 默认初始化 ...
        requestMatcher = new AntPathRequestMatcher(applicationAuthServerUtils.getProperties().getServerMetaDataEndpointConfig().getOpenConnectIdMetadataEndpointUri());
    }

    @Override
    public <B extends HttpSecurityBuilder<B>> void configure(B builder) {
        ApplicationAuthServerUtils authServerProperties = ApplicationAuthServerUtils.getApplicationAuthServerProperties(builder);
        LightningCentralAuthServer centralAuthServerConfigurer = builder.getSharedObject(LightningCentralAuthServer.class);
        String uri = authServerProperties.getProperties().getServerMetaDataEndpointConfig().getOpenConnectIdMetadataEndpointUri();
        if(centralAuthServerConfigurer != null) {
            // 避免和 central auth server 相关配置冲突 ..
            requestMatcher = new AntPathRequestMatcher(authServerProperties.getFullConfigProperties().getServerMetaDataEndpointConfig().getOpenConnectIdMetadataEndpointUri());
            uri = authServerProperties.getFullConfigProperties().getServerMetaDataEndpointConfig().getOpenConnectIdMetadataEndpointUri();
        }

        // 是否启用 ..
        OidcProviderConfigurationEndpointFilter configurationEndpointFilter = new OidcProviderConfigurationEndpointFilter(
                AppAuthConfigurerUtils.getProviderSettings(builder).getProviderSettings(),
                uri,
                authServerProperties.getProperties().getServerMetaDataEndpointConfig().isEnableOidc()
        );

        builder.addFilterBefore(configurationEndpointFilter, AbstractPreAuthenticatedProcessingFilter.class);
    }

    @Override
    public RequestMatcher getRequestMatcher() {
        return requestMatcher;
    }

}
