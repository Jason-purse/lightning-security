package com.generatera.central.oauth2.authorization.server.configuration;


import com.generatera.authorization.server.common.configuration.AuthConfigConstant;
import com.generatera.authorization.server.common.configuration.AuthorizationServerCommonComponentsConfiguration;
import com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties;
import com.generatera.authorization.server.common.configuration.LightningAuthServerConfigurer;
import com.generatera.security.authorization.server.specification.util.LogUtil;
import com.generatera.central.oauth2.authorization.server.configuration.components.authentication.DefaultOAuth2CentralServerAuthenticationSuccessHandler;
import com.generatera.central.oauth2.authorization.server.configuration.components.authentication.LightningOAuth2CentralServerAuthenticationSuccessHandler;
import com.generatera.central.oauth2.authorization.server.configuration.components.authorization.OptimizedForOAuth2AuthorizationEndpointFilter;
import com.generatera.central.oauth2.authorization.server.configuration.components.token.DefaultOpaqueAwareOAuth2TokenCustomizer;
import com.generatera.central.oauth2.authorization.server.configuration.components.token.DefaultTokenDetailAwareOAuth2TokenCustomizer;
import com.generatera.central.oauth2.authorization.server.configuration.components.token.DelegateCentralOauth2TokenCustomizer;
import com.generatera.central.oauth2.authorization.server.configuration.components.token.LightningCentralOAuth2TokenCustomizer;
import com.generatera.central.oauth2.authorization.server.configuration.components.token.LightningCentralOAuth2TokenCustomizer.LightningCentralOAuth2AccessTokenCustomizer;
import com.generatera.central.oauth2.authorization.server.configuration.components.token.LightningCentralOAuth2TokenCustomizer.LightningCentralOAuth2JwtTokenCustomizer;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JWKSourceProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * oauth2 central authorization server com.generatera.oauth2.resource.server.config
 * <p>
 * 1. oauth2 central authorization server??? ?????????????????????
 * 1.1 clientRegistrationRepository
 * 2. token customizer(built-in)
 * <p>
 * 3. oauth2 central authServer ext customizers
 * <p>
 * ????????????????????? oauth2 ??????????????? ?????????????????????
 * ????????????????????????????????????????????????????????????(??????????????????,??????????????????????????????????????????????????????) ...
 * 4. ?????????????????? token ????????????(?????????token?????? ??? oauth2 token?????????????????????) ...
 * 5. ?????????????????????????????????(????????????????????????)
 *
 * @see org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
 * @see com.generatera.central.oauth2.authorization.server.configuration.components.token.LightningCentralOAuth2TokenCustomizer
 * @see LightningOAuth2CentralAuthorizationServerBootstrapConfigurer
 * @see DefaultOpaqueAwareOAuth2TokenCustomizer
 * @see DefaultTokenDetailAwareOAuth2TokenCustomizer
 */
@Slf4j
@Configuration
@AutoConfiguration
@AutoConfigureBefore({AuthorizationServerCommonComponentsConfiguration.class})
@EnableConfigurationProperties({OAuth2CentralAuthorizationServerProperties.class})
@Import(OAuth2CentralAuthorizationServerCCImportSelector.class)
@RequiredArgsConstructor
public class OAuth2CentralAuthorizationServerConfiguration {

    private final OAuth2CentralAuthorizationServerProperties properties;

    /**
     * ?????????????????? ...
     */
    @Bean
    public ProviderSettings providerSettings() {
        return properties.getProvider().getOAuth2ProviderSettingsProvider();
    }


    // // TODO: 2023/2/2  ??????????????????????????????????????????

    ///**
    // * ??????,?????? oauth2 token settings provider ..
    // * ??????????????????provider ??????
    // */
    //@Bean
    //@Qualifier("oauth2_token_setting_provider")
    //public TokenSettingsProvider oauth2TokenSettingProvider(TokenSettingsProvider tokenSettingsProvider) {
    //    TokenSettingsProperties tokenSettings = tokenSettingsProvider.getTokenSettings();
    //    return new TokenSettingsProvider(
    //            OAuth2ServerTokenSettings.withSettings(tokenSettings.getSettings())
    //                    .build()
    //    );
    //}


    // -------------------------- token customizer -----------------------------------------------------------------
    // ------------------------ access token ????????? --------------------------
    // ??????,????????? authorization-server????????? token ???????????????????????? ...

    @Bean
    @Primary
    @ConditionalOnBean(LightningCentralOAuth2AccessTokenCustomizer.class)
    public LightningCentralOAuth2TokenCustomizer<OAuth2TokenClaimsContext> pluginInCentralOAuth2TokenCustomizer(
            AuthorizationServerComponentProperties properties,
            LightningCentralOAuth2AccessTokenCustomizer tokenCustomizer
    ) {

        DefaultOpaqueAwareOAuth2TokenCustomizer defaultOpaqueAwareOAuth2TokenCustomizer = new DefaultOpaqueAwareOAuth2TokenCustomizer();

        return new DelegateCentralOauth2TokenCustomizer<>(
                tokenCustomizer,
                new DelegateCentralOauth2TokenCustomizer<>(
                        // ???????????????
                        new DefaultTokenDetailAwareOAuth2TokenCustomizer(properties.getTokenSettings()),
                        defaultOpaqueAwareOAuth2TokenCustomizer
                )::customize
        );
    }


    @Bean
    @Primary
    @ConditionalOnMissingBean(LightningCentralOAuth2AccessTokenCustomizer.class)
    public LightningCentralOAuth2TokenCustomizer<OAuth2TokenClaimsContext> centralOAuth2TokenCustomizer(
            AuthorizationServerComponentProperties properties
    ) {
        DefaultOpaqueAwareOAuth2TokenCustomizer defaultOpaqueAwareOAuth2TokenCustomizer = new DefaultOpaqueAwareOAuth2TokenCustomizer();

        return new DelegateCentralOauth2TokenCustomizer<>(
                // ???????????????
                new DefaultTokenDetailAwareOAuth2TokenCustomizer(properties.getTokenSettings())::customize,
                defaultOpaqueAwareOAuth2TokenCustomizer::customize
        );
    }

    @Bean
    @Primary
    @ConditionalOnBean(LightningCentralOAuth2JwtTokenCustomizer.class)
    public LightningCentralOAuth2TokenCustomizer<JwtEncodingContext> pluginCentralOAuth2JwtTokenCustomizer(
            AuthorizationServerComponentProperties properties,
            LightningCentralOAuth2JwtTokenCustomizer tokenCustomizer
    ) {

        DefaultOpaqueAwareOAuth2TokenCustomizer defaultOpaqueAwareOAuth2TokenCustomizer = new DefaultOpaqueAwareOAuth2TokenCustomizer();
        return new DelegateCentralOauth2TokenCustomizer<>(
                tokenCustomizer,
                new DelegateCentralOauth2TokenCustomizer<>(
                        // ???????????????
                        new DefaultTokenDetailAwareOAuth2TokenCustomizer(properties.getTokenSettings())::customize,
                        defaultOpaqueAwareOAuth2TokenCustomizer::customize
                )
        );
    }


    @Bean
    @Primary
    @ConditionalOnMissingBean(LightningCentralOAuth2JwtTokenCustomizer.class)
    public LightningCentralOAuth2TokenCustomizer<JwtEncodingContext> centralOAuth2JwtTokenCustomizer(
            AuthorizationServerComponentProperties properties
    ) {
        DefaultOpaqueAwareOAuth2TokenCustomizer defaultOpaqueAwareOAuth2TokenCustomizer = new DefaultOpaqueAwareOAuth2TokenCustomizer();
        return new DelegateCentralOauth2TokenCustomizer<>(
                // ???????????????
                new DefaultTokenDetailAwareOAuth2TokenCustomizer(properties.getTokenSettings())::customize,
                defaultOpaqueAwareOAuth2TokenCustomizer::customize
        );
    }


    /**
     * ?????? oauth2 authorization server configurer ..
     * ???????????? oauth2 ???????????????????????????????????????,??????{@link LightningOAuth2CentralAuthorizationServerBootstrapConfigurer} ????????????,
     * ??????????????????????????????@Order????????????Ordered??????????????????????????????,???????????????{@link org.springframework.core.annotation.AnnotationAwareOrderComparator}
     * ??????????????????????????? ??????,???????????????????????????,??????????????? ????????????????????? ...
     *
     * @param extConfigurers ????????? oauth2 authorization server ????????????(?????? ??????????????????????????????,??????password) ..
     *                       ???????????? ?????? lightning-central-oauth2-authorization-password-grant-support-server ..
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    @SuppressWarnings("unchecked")
    public LightningAuthServerConfigurer configurer(
            LightningCentralOAuth2TokenCustomizer<OAuth2TokenClaimsContext> accessTokenCustomizer,
            LightningCentralOAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer,
            @Autowired(required = false)
                    List<LightningOAuth2CentralAuthorizationServerBootstrapConfigurer> extConfigurers,
            @Autowired(required = false)
                    LightningOAuth2CentralServerAuthenticationSuccessHandler successHandler
    ) {
        return new LightningAuthServerConfigurer() {
            @Override
            public void configure(HttpSecurity securityBuilder) throws Exception {


                OAuth2AuthorizationServerConfigurer<HttpSecurity> configurer = securityBuilder.getConfigurer(OAuth2AuthorizationServerConfigurer.class);
                // ?????????????????? ..
                if (configurer == null) {
                    configurer
                            = OAuth2AuthorizationServerConfigurerExtUtils.getOAuth2AuthorizationServerConfigurer(securityBuilder);
                    securityBuilder.apply(configurer);
                }

                formLoginSupport(securityBuilder);

                securityBuilder.setSharedObject(LightningCentralOAuth2JwtTokenCustomizer.class,jwtTokenCustomizer::customize);
                securityBuilder.setSharedObject(LightningCentralOAuth2AccessTokenCustomizer.class,accessTokenCustomizer::customize);
                // ???????????????
                configurer.tokenGenerator(
                        OAuth2AuthorizationServerConfigurerExtUtils.getTokenGenerator(
                        securityBuilder
                ));
                // ????????????
                if (!CollectionUtils.isEmpty(extConfigurers)) {
                    for (LightningOAuth2CentralAuthorizationServerBootstrapConfigurer extConfigurer : extConfigurers) {
                        extConfigurer.configure(configurer);
                    }
                }

                // ????????????????????????csrf ??????
                securityBuilder.csrf()
                        .ignoringRequestMatchers(configurer.getEndpointsMatcher());

                // ?????????????????????????????????token
                // ????????????jwt, ?????????????????????????????????????????? ..
                securityBuilder
                        .oauth2ResourceServer()
                        .jwt();
            }

            private void formLoginSupport(HttpSecurity securityBuilder) throws Exception {

                // ?????? ??????????????? ...
                securityBuilder.setSharedObject(AuthConfigConstant.ENABLE_FORM_LOGIN_NO_SEPARATION.class, AuthConfigConstant.ENABLE_FORM_LOGIN_NO_SEPARATION.INSTANCE);
                // ???????????? ...
                // ??????????????????
                FormLoginConfigurer<HttpSecurity> formLoginConfigurer = securityBuilder.formLogin();
                OAuth2CentralAuthorizationServerProperties.FormLoginSupportConfig config = properties.getFormLoginConfig();
                if (StringUtils.hasText(config.getLoginPageUrl())) {
                    formLoginConfigurer.loginPage(config.getLoginPageUrl());
                }
                if (StringUtils.hasText(config.getLoginProcessUrl())) {
                    formLoginConfigurer.loginProcessingUrl(config.getLoginProcessUrl());
                }

                // default success url ???????????????,?????????????????????,????????????????????????????????? ??????????????????????????? ..
                if (StringUtils.hasText(config.getDefaultSuccessForwardUrl())) {
                    formLoginConfigurer.defaultSuccessUrl(config.getDefaultSuccessForwardUrl());
                }

                // ?????? ?????????????????????????????? ???????????? ...
                OptimizedForOAuth2AuthorizationEndpointFilter forOAuth2AuthorizationEndpointFilter = new OptimizedForOAuth2AuthorizationEndpointFilter();
                if(StringUtils.hasText(config.getLoginPageUrl())) {
                    // ????????????
                    forOAuth2AuthorizationEndpointFilter.setLoginPageUrl(config.getLoginPageUrl());
                }

                // ?????????????????? ... ??????????????? ...
                securityBuilder.addFilterBefore(forOAuth2AuthorizationEndpointFilter, AbstractPreAuthenticatedProcessingFilter.class);


                // ?????????????????? , ??????????????????????????? ....
                formLoginConfigurer
                        .addObjectPostProcessor(new ObjectPostProcessor<AbstractAuthenticationProcessingFilter>() {
                            @Override
                            public <O extends AbstractAuthenticationProcessingFilter> O postProcess(O filter) {
                                DefaultOAuth2CentralServerAuthenticationSuccessHandler handler = new DefaultOAuth2CentralServerAuthenticationSuccessHandler();
                                if (successHandler != null) {
                                    handler.setAuthenticationSuccessHandler(successHandler);
                                }
                                filter.setAuthenticationSuccessHandler(handler);
                                return filter;
                            }
                        });

                securityBuilder.sessionManagement()
                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);

                LogUtil.prettyLog("central oauth2 auth server enable form login noSeparation support !!!");
            }
        };
    }


    @Bean
    public JwtDecoder jwtDecoder(JWKSourceProvider provider) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(provider.getJWKSource());
    }

}
