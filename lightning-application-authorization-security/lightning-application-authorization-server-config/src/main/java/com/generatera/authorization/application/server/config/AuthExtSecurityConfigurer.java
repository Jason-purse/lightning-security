package com.generatera.authorization.application.server.config;

import com.generatera.authorization.server.common.configuration.OAuth2AuthorizationServer;
import com.generatera.authorization.server.common.configuration.provider.AuthorizationServerNimbusJwkSetEndpointFilter;
import com.generatera.authorization.server.common.configuration.provider.ProviderExtUtils;
import com.generatera.authorization.server.common.configuration.provider.metadata.AuthorizationProviderContextFilter;
import com.generatera.authorization.server.common.configuration.provider.metadata.AuthorizationServerMetadataEndpointFilter;
import com.generatera.security.authorization.server.specification.ProviderSettingsProvider;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettings;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import java.net.URI;
import java.util.List;

/**
 * 实现 授权服务器的启动 ...
 */
@Slf4j
public class AuthExtSecurityConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>  {

    private final List<LightningAppAuthServerConfigurer> configurers;


    public AuthExtSecurityConfigurer(List<LightningAppAuthServerConfigurer> configurerList) {
        this.configurers = configurerList;
    }

    @Override
    public void init(HttpSecurity builder) throws Exception {

        ApplicationAuthExtConfigurerUtils.getJwkSourceProvider(builder);
        for (LightningAppAuthServerConfigurer configurer : configurers) {
            configurer.configure(builder);
        }
        OAuth2AuthorizationServer oAuth2AuthorizationServer = builder.getSharedObject(OAuth2AuthorizationServer.class);
        // 如果不等于 null,则表示oauth2 已经启动 ...
        // 否则填充 一些公共配置(用于 可选的resource server 进一步配置) ..
        if(oAuth2AuthorizationServer == null) {
            oauth2CommonComponentFill(builder);
            oauth2CommonComponentValidation(builder);
        }
    }



    private void oauth2CommonComponentValidation(HttpSecurity builder) {
        ProviderSettingsProvider providerSettings = ProviderExtUtils.getProviderSettings(builder);
        validateProviderSettings(providerSettings.getProviderSettings());
    }

    @Override
    public void configure(HttpSecurity builder) throws Exception {
        // 如果启用,则 providerSettings 已经被 authorization-server-oauth2-config 处理 ..(当 oauth2 server启用时)
//        if(!oauth2ServerEnabled) {
//            // 启用 oauth2 部分公共组件配置 ..
//            oauth2CommonComponentFill(builder);
//        }
        /*String authorizationRequestBaseUri = this.authorizationExtEndpointConfig.authorizationExtRequestBaseUri;
        if (authorizationRequestBaseUri == null) {
            authorizationRequestBaseUri = "/oauth2/authorization";
        }
        OAuth2AuthorizationRequestAndExtRedirectFilter authorizationExtRequestFilter = new OAuth2AuthorizationRequestAndExtRedirectFilter(OAuth2ClientConfigurerExtUtils.getClientRegistrationRepository((HttpSecurityBuilder)this.getBuilder()), authorizationRequestBaseUri);
        if(this.authorizationExtEndpointConfig.authorizationExtRequestResolver != null) {
            authorizationExtRequestFilter.setAuth2AuthorizationExtRequestResolver(this.authorizationExtEndpointConfig.authorizationExtRequestResolver);
        }

        builder.addFilterBefore(authorizationExtRequestFilter, OAuth2AuthorizationRequestRedirectFilter.class);*/
    }

    // oauth2 通用组件复用 ..
    private void oauth2CommonComponentFill(HttpSecurity builder) {

        ProviderSettingsProvider providerSettings = ProviderExtUtils.getProviderSettings(builder);
        AuthorizationProviderContextFilter providerContextFilter
                = new AuthorizationProviderContextFilter(providerSettings.getProviderSettings());
        builder.addFilterAfter(this.postProcess(providerContextFilter), SecurityContextPersistenceFilter.class);
        JWKSource<SecurityContext> jwkSource = ProviderExtUtils.getJwkSource(builder);

        // jwk source ..
        if (jwkSource != null) {
            AuthorizationServerNimbusJwkSetEndpointFilter jwkSetEndpointFilter
                    = new AuthorizationServerNimbusJwkSetEndpointFilter(jwkSource,
                    providerSettings.getProviderSettings().getJwkSetEndpoint());
            builder.addFilterBefore(
                    this.postProcess(jwkSetEndpointFilter),
                    AbstractPreAuthenticatedProcessingFilter.class);
        }

        AuthorizationServerMetadataEndpointFilter authorizationServerMetadataEndpointFilter =
                new AuthorizationServerMetadataEndpointFilter(providerSettings.getProviderSettings());
        builder.addFilterBefore(
                this.postProcess(authorizationServerMetadataEndpointFilter),
                AbstractPreAuthenticatedProcessingFilter.class);
    }


    private static void validateProviderSettings(ProviderSettings providerSettings) {
        if (providerSettings.getIssuer() != null) {
            URI issuerUri;
            try {
                issuerUri = new URI(providerSettings.getIssuer());
                issuerUri.toURL();
            } catch (Exception var3) {
                throw new IllegalArgumentException("issuer must be a valid URL", var3);
            }

            if (issuerUri.getQuery() != null || issuerUri.getFragment() != null) {
                throw new IllegalArgumentException("issuer cannot contain query or fragment component");
            }
        }

    }


}
