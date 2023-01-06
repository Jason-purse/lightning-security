package com.generatera.authorization.application.server.config;

import com.generatera.authorization.application.server.config.specification.BasedNimbusJwkSetForSpecificationEndpointFilter;
import com.generatera.authorization.application.server.config.specification.BasedOAuth2AuthorizationServerForSpecificationMetadataEndpointFilter;
import com.generatera.authorization.application.server.config.specification.BasedProviderForSpecificationContextFilter;
import com.generatera.authorization.server.common.configuration.provider.ProviderExtUtils;
import com.generatera.authorization.server.common.configuration.provider.ProviderSettings;
import com.generatera.authorization.server.common.configuration.provider.ProviderSettingsProvider;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
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



    private final ApplicationAuthServerProperties properties;

    private final List<LightningAppAuthServerConfigurer> configurers;

    /**
     * 判断 oauth2 server 是否启用 ...
     */
    private Boolean oauth2ServerEnabled = false;

    private Boolean authenticationLoginEnabled = false;


    public AuthExtSecurityConfigurer(ApplicationAuthServerProperties properties,
                                     @Autowired(required = false) List<LightningAppAuthServerConfigurer> configurerList) {
        this.properties = properties;
        this.configurers = configurerList;
    }

    @Override
    public void init(HttpSecurity builder) throws Exception {

        // 当启用oauth2AuthServer并存在对应的配置器时,则进行 oauth2 server配置委托 ..
        // oauth server  放到第一位
        if(properties.getOA2AuthServer().isEnable()) {
            List<LightningAppAuthServerConfigurer> serverConfigurers = configurers.stream().filter(ele -> ele instanceof LightningOAuth2ServerConfigurer).toList();
            if(serverConfigurers.size() > 0) {
                oauth2ServerEnabled = true;
                OAuth2AuthorizationServerConfigurer<HttpSecurity> oAuth2AuthorizationServerConfigurer
                        = new OAuth2AuthorizationServerConfigurer<>();
                    builder.apply(oAuth2AuthorizationServerConfigurer);
                for (LightningAppAuthServerConfigurer serverConfigurer : serverConfigurers) {
                    serverConfigurer.configure(oAuth2AuthorizationServerConfigurer);
                }
                log.info("OAuth2 Authorization Server is enabled !!!");
            }
            else {
                log.info("The current OAuth2 auth server is disabled. Although Oauth2 auth server has been enabled,because no dependencies exists !!!");
            }
        }
        else {
            log.info("The current OAuth2 auth server is disabled.");
        }


        // oauth2 login
        if(properties.oauth2Login.isEnable()) {
            authenticationLoginEnabled = true;
            List<LightningAppAuthServerConfigurer> collect = configurers.stream()
                    .filter(ele -> ele instanceof LightningOAuth2LoginConfigurer)
                    .toList();
            if(collect.size() > 0) {
                // oauth2 Login 在这里处理 ..
                OAuth2LoginConfigurer<HttpSecurity> auth2LoginConfigurer = builder.oauth2Login();
                for (LightningAppAuthServerConfigurer lightningAppAuthServerConfigurer : collect) {
                    lightningAppAuthServerConfigurer.configure(auth2LoginConfigurer);
                }
                log.info("The current OAUth2 login is enabled!!!");
            }else {
                log.info("The current OAuth2 login is disabled. Although Oauth2 login has been enabled,because no dependencies exists !!!");
            }
        }
        else {
            log.info("The current OAuth2 login is disabled .");
        }


        // form login
        if(properties.formLogin.isEnable()) {
            if(ObjectUtils.isNotEmpty(configurers)) {
                List<LightningAppAuthServerConfigurer> collect = configurers.stream()
                        .filter(ele -> ele instanceof LightningFormLoginConfigurer).toList();

                if(collect.size() > 0) {
                    FormLoginConfigurer<HttpSecurity> formLoginConfigurer = builder.formLogin();
                    for (LightningAppAuthServerConfigurer lightningAppAuthServerConfigurer : collect) {
                        lightningAppAuthServerConfigurer.configure(formLoginConfigurer);
                    }
                    log.info("The current Form login is enabled !!!");
                }
                else {
                    log.info("The current Form login is disabled. Although Form login has been enabled,because no dependencies exists !!!");
                }

            }
        }
        else {
            log.info("The current Form login is disabled.");
        }

        // todo
        if(properties.lcdpLogin.isEnable()) {
            // lcdp
            log.info("Lcdp login is not  currently supported  !!!");
        }
        else {
            log.info("Lcdp login is not currently supported !!!");
        }

        if(!oauth2ServerEnabled) {
            // 就开始基于 oauth2CommonComponent校验 .
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
        if(!oauth2ServerEnabled) {
            // 启用 oauth2 部分公共组件配置 ..
            oauth2CommonComponentFill(builder);
        }
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
        BasedProviderForSpecificationContextFilter providerContextFilter
                = new BasedProviderForSpecificationContextFilter(providerSettings.getProviderSettings());
        builder.addFilterAfter(this.postProcess(providerContextFilter), SecurityContextPersistenceFilter.class);
        JWKSource<SecurityContext> jwkSource = ProviderExtUtils.getJwkSource(builder);

        // jwk source ..
        if (jwkSource != null) {
            BasedNimbusJwkSetForSpecificationEndpointFilter jwkSetEndpointFilter
                    = new BasedNimbusJwkSetForSpecificationEndpointFilter(jwkSource,
                    providerSettings.getProviderSettings().getJwkSetEndpoint());
            builder.addFilterBefore(
                    this.postProcess(jwkSetEndpointFilter),
                    AbstractPreAuthenticatedProcessingFilter.class);
        }

        BasedOAuth2AuthorizationServerForSpecificationMetadataEndpointFilter authorizationServerMetadataEndpointFilter =
                new BasedOAuth2AuthorizationServerForSpecificationMetadataEndpointFilter(providerSettings.getProviderSettings());
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
