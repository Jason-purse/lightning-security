package com.generatera.authorization.application.server.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.web.DefaultSecurityFilterChain;

import java.util.List;

/**
 * 实现 oauth2 login 扩展
 */
@Slf4j
public class OAuth2ExtSecurityConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {



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
            log.info("The current Form login is enabled !!!");
            FormLoginConfigurer<HttpSecurity> formLoginConfigurer = builder.formLogin();
            if(ObjectUtils.isNotEmpty(configurers)) {
                List<LightningAppAuthServerConfigurer> collect = configurers.stream()
                        .filter(ele -> ele instanceof LightningFormLoginConfigurer).toList();
                for (LightningAppAuthServerConfigurer lightningAppAuthServerConfigurer : collect) {
                    lightningAppAuthServerConfigurer.configure(formLoginConfigurer);
                }
            }
        }

        if(properties.oauth2Login.isEnable()) {
            log.info("The current OAUth2 login is enabled!!!");
            // oauth2 Login 在这里处理 ..
            OAuth2LoginConfigurer<HttpSecurity> auth2LoginConfigurer = builder.oauth2Login();
            List<LightningAppAuthServerConfigurer> collect = configurers.stream()
                    .filter(ele -> ele instanceof LightningOAuth2LoginConfigurer)
                    .toList();

            for (LightningAppAuthServerConfigurer lightningAppAuthServerConfigurer : collect) {
                lightningAppAuthServerConfigurer.configure(auth2LoginConfigurer);
            }
        }

        if(properties.lcdpLogin.isEnable()) {
            // lcdp
            log.info("Lcdp login is not  currently supported  !!!");
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
