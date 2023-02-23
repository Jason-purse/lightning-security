package com.generatera.central.oauth2.authorization.server.configuration;


import com.generatera.authorization.server.common.configuration.AuthConfigConstant;
import com.generatera.authorization.server.common.configuration.AuthorizationServerCommonComponentsConfiguration;
import com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties;
import com.generatera.authorization.server.common.configuration.LightningAuthServerConfigurer;
import com.generatera.authorization.server.common.configuration.util.LogUtil;
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
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * oauth2 central authorization server com.generatera.oauth2.resource.server.config
 * <p>
 * 1. oauth2 central authorization server的 一部分组件注册
 * 1.1 clientRegistrationRepository
 * 2. token customizer(built-in)
 * <p>
 * 3. oauth2 central authServer ext customizers
 * <p>
 * 也就是作为中央 oauth2 授权服务器 提供额外的配置
 * 首先就是它包含了已经注册的客户端列表信息(需要一个仓库,但是普通的授权服务器不可能存在此东西) ...
 * 4. 也就是自己的 token 自定义器(普通的token生成 和 oauth2 token生成存在差异性) ...
 * 5. 同样也需要授权协商仓库(保留授权协商信息)
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
     * 需要进行补充 ...
     */
    @Bean
    public ProviderSettings providerSettings() {
        return properties.getProvider().getOAuth2ProviderSettingsProvider();
    }


    // // TODO: 2023/2/2  中央授权服务器不需要这些东西

    ///**
    // * 表示,使用 oauth2 token settings provider ..
    // * 增加了额外的provider 信息
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
    // ------------------------ access token 定制器 --------------------------
    // 重写,不需要 authorization-server提供的 token 定制器的默认配置 ...

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
                        // 顺序很重要
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
                // 顺序很重要
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
                        // 顺序很重要
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
                // 顺序很重要
                new DefaultTokenDetailAwareOAuth2TokenCustomizer(properties.getTokenSettings())::customize,
                defaultOpaqueAwareOAuth2TokenCustomizer::customize
        );
    }


    /**
     * 支持 oauth2 authorization server configurer ..
     * 并且支持 oauth2 授权服务器的额外配置且扩展,通过{@link LightningOAuth2CentralAuthorizationServerBootstrapConfigurer} 进行配置,
     * 同时它本质上可以通过@Order注解或者Ordered接口进行执行顺序处理,默认是通过{@link org.springframework.core.annotation.AnnotationAwareOrderComparator}
     * 进行顺序比较器处理 顺序,如果没有优先级要求,默认追加到 配置器列表尾部 ...
     *
     * @param extConfigurers 并支持 oauth2 authorization server 扩展配置(例如 支持自定义的授权方式,例如password) ..
     *                       详情查看 模块 lightning-central-oauth2-authorization-password-grant-support-server ..
     */
    @Bean
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
                // 启动这个配置 ..
                if (configurer == null) {
                    configurer
                            = OAuth2AuthorizationServerConfigurerExtUtils.getOAuth2AuthorizationServerConfigurer(securityBuilder);
                    securityBuilder.apply(configurer);
                }

                formLoginSupport(securityBuilder);

                securityBuilder.setSharedObject(LightningCentralOAuth2JwtTokenCustomizer.class,jwtTokenCustomizer::customize);
                securityBuilder.setSharedObject(LightningCentralOAuth2AccessTokenCustomizer.class,accessTokenCustomizer::customize);
                // 强制自定义
                configurer.tokenGenerator(
                        OAuth2AuthorizationServerConfigurerExtUtils.getTokenGenerator(
                        securityBuilder
                ));
                // 增加扩展
                if (!CollectionUtils.isEmpty(extConfigurers)) {
                    for (LightningOAuth2CentralAuthorizationServerBootstrapConfigurer extConfigurer : extConfigurers) {
                        extConfigurer.configure(configurer);
                    }
                }

                // 忽略对这些端点的csrf 处理
                securityBuilder.csrf()
                        .ignoringRequestMatchers(configurer.getEndpointsMatcher());

                // 最后增加解码器解码访问token
                // 默认使用jwt, 因为只需要将用户信息传出即可 ..
                securityBuilder
                        .oauth2ResourceServer()
                        .jwt();
            }

            private void formLoginSupport(HttpSecurity securityBuilder) throws Exception {

                // 表示 非分离配置 ...
                securityBuilder.setSharedObject(AuthConfigConstant.ENABLE_FORM_LOGIN_NO_SEPARATION.class, AuthConfigConstant.ENABLE_FORM_LOGIN_NO_SEPARATION.INSTANCE);
                // 表单处理 ...
                // 增加表单支持
                FormLoginConfigurer<HttpSecurity> formLoginConfigurer = securityBuilder.formLogin();
                OAuth2CentralAuthorizationServerProperties.FormLoginSupportConfig config = properties.getFormLoginConfig();
                if (StringUtils.hasText(config.getLoginPageUrl())) {
                    formLoginConfigurer.loginPage(config.getLoginPageUrl());
                }
                if (StringUtils.hasText(config.getLoginProcessUrl())) {
                    formLoginConfigurer.loginProcessingUrl(config.getLoginProcessUrl());
                }

                // default success url 不总是使用,在进行授权码时,进行用户登陆之后跳转为 为进行完毕的授权码 ..
                if (StringUtils.hasText(config.getDefaultSuccessForwardUrl())) {
                    formLoginConfigurer.defaultSuccessUrl(config.getDefaultSuccessForwardUrl());
                }

                // 增加 授权码流程未登录用户 信息感知 ...
                OptimizedForOAuth2AuthorizationEndpointFilter forOAuth2AuthorizationEndpointFilter = new OptimizedForOAuth2AuthorizationEndpointFilter();
                if(StringUtils.hasText(config.getLoginPageUrl())) {
                    // 必须设置
                    forOAuth2AuthorizationEndpointFilter.setLoginPageUrl(config.getLoginPageUrl());
                }

                // 在它之前增加 ... 这个过滤器 ...
                securityBuilder.addFilterBefore(forOAuth2AuthorizationEndpointFilter, AbstractPreAuthenticatedProcessingFilter.class);


                // 实现后置处理 , 修改默认的认证端点 ....
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
