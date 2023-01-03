package com.generatera.authorization.application.server.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.DefaultSecurityFilterChain;

import java.util.List;

/**
 * 实现 oauth2 login 扩展
 */
@Slf4j
public class OAuth2ExtSecurityConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>  {



    private final ApplicationAuthServerProperties properties;

    private final List<LightningAppAuthServerConfigurer> configurers;

    public OAuth2ExtSecurityConfigurer(ApplicationAuthServerProperties properties,
                                       @Autowired(required = false) List<LightningAppAuthServerConfigurer> configurerList) {
        this.properties = properties;
        this.configurers = configurerList;
    }

    @Override
    public void init(HttpSecurity builder) throws Exception {

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

        if(properties.oauth2Login.isEnable()) {
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

        if(properties.lcdpLogin.isEnable()) {
            // lcdp
            log.info("Lcdp login is not  currently supported  !!!");
        }
        else {
            log.info("Lcdp login is not currently supported !!!");
        }

        if(properties.getOA2AuthServer().isEnable()) {
            List<LightningAppAuthServerConfigurer> serverConfigurers = configurers.stream().filter(ele -> ele instanceof LightningOAuth2ServerConfigurer).toList();
            if(configurers.size() > 0) {
                OAuth2AuthorizationServerConfigurer<HttpSecurity> oAuth2AuthorizationServerConfigurer
                        = new OAuth2AuthorizationServerConfigurer<>();
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

    }

    @Override
    public void configure(HttpSecurity builder) throws Exception {
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






}
