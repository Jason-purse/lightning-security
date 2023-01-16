package com.generatera.central.oauth2.authorization.server.configuration;


import com.generatera.authorization.application.server.config.ApplicationAuthServerConfig;
import com.generatera.authorization.application.server.config.LightningAppAuthServerConfigurer;
import com.generatera.authorization.application.server.form.login.config.ApplicationFormLoginConfiguration;
import com.generatera.authorization.application.server.form.login.config.FormLoginProperties;
import com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties;
import com.generatera.central.oauth2.authorization.server.configuration.components.token.DefaultOpaqueAwareOAuth2TokenCustomizer;
import com.generatera.central.oauth2.authorization.server.configuration.components.token.DefaultTokenDetailAwareOAuth2TokenCustomizer;
import com.generatera.central.oauth2.authorization.server.configuration.components.token.DelegateCentralOauth2TokenCustomizer;
import com.generatera.central.oauth2.authorization.server.configuration.components.token.LightningCentralOAuth2TokenCustomizer;
import com.generatera.central.oauth2.authorization.server.configuration.components.token.LightningCentralOAuth2TokenCustomizer.LightningCentralOAuth2AccessTokenCustomizer;
import com.generatera.central.oauth2.authorization.server.configuration.components.token.LightningCentralOAuth2TokenCustomizer.LightningCentralOAuth2JwtTokenCustomizer;
import com.generatera.security.authorization.server.specification.ProviderSettingsProvider;
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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * oauth2 central authorization server com.generatera.oauth2.resource.server.config
 * <p>
 * 1. oauth2 central authorization server的 一部分组件注册
 * 1.1 clientRegistrationRepository
 * 2. token customizer(built-in)
 * <p>
 * 3. oauth2 central authServer ext customizers
 *
 * 也就是作为中央 oauth2 授权服务器 提供额外的配置
 * 首先就是它包含了已经注册的客户端列表信息(需要一个仓库,但是普通的授权服务器不可能存在此东西) ...
 * 4. 也就是自己的 token 自定义器(暂时,先分离配置,后续通过 lightning-token-generator 兼容到  oauth2 token -generator)
 * 5. 同样也需要授权协商仓库(保留授权协商信息)
 *
 *
 * // TODO: 2023/1/16  通过 lightning-security-authorization-server-specification 的规定信息进行 oauth2 兼容 ..
 *
 *
 * @see org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
 * @see com.generatera.central.oauth2.authorization.server.configuration.components.token.LightningCentralOAuth2TokenCustomizer
 * @see LightningOAuth2CentralAuthorizationServerExtConfigurer
 * @see DefaultOpaqueAwareOAuth2TokenCustomizer
 * @see DefaultTokenDetailAwareOAuth2TokenCustomizer
 */
@Slf4j
@Configuration
@AutoConfiguration
@AutoConfigureBefore({ApplicationAuthServerConfig.class, ApplicationFormLoginConfiguration.class})
@EnableConfigurationProperties({OAuth2CentralAuthorizationServerProperties.class, FormLoginProperties.class})
@Import(OAuth2CentralAuthorizationServerCCImportSelector.class)
public class OAuth2CentralAuthorizationServerConfiguration {


    @Bean
    public ProviderSettings providerSettings(ProviderSettingsProvider provider) {
        com.generatera.security.authorization.server.specification.components.provider.ProviderSettings
                providerSettings = provider.getProviderSettings();

        // 转交给 oauth2 ..
        return ProviderSettings.withSettings(providerSettings.getSettings()).build();
    }


    // -------------------------- token customizer -----------------------------------------------------------------

    @Bean
    @ConditionalOnBean(LightningCentralOAuth2AccessTokenCustomizer.class)
    public LightningCentralOAuth2TokenCustomizer<OAuth2TokenClaimsContext> pluginInCentralOAuth2TokenCustomizer(
            AuthorizationServerComponentProperties properties,
            LightningCentralOAuth2AccessTokenCustomizer tokenCustomizer
    ) {
        return new DelegateCentralOauth2TokenCustomizer<>(
                tokenCustomizer,
                centralOAuth2TokenCustomizer(properties)
        );
    }


    @Bean
    @ConditionalOnMissingBean(LightningCentralOAuth2AccessTokenCustomizer.class)
    public LightningCentralOAuth2TokenCustomizer<OAuth2TokenClaimsContext> centralOAuth2TokenCustomizer(
            AuthorizationServerComponentProperties properties
    ) {
        DefaultOpaqueAwareOAuth2TokenCustomizer defaultOpaqueAwareOAuth2TokenCustomizer = new DefaultOpaqueAwareOAuth2TokenCustomizer();
        defaultOpaqueAwareOAuth2TokenCustomizer.setValueTypeFormat(properties.getTokenSettings().getTokenValueFormat());

        return new DelegateCentralOauth2TokenCustomizer<>(
                // 顺序很重要
                new DefaultTokenDetailAwareOAuth2TokenCustomizer(properties.getTokenSettings())::customize,
                defaultOpaqueAwareOAuth2TokenCustomizer::customize
        );
    }


    @Bean
    @ConditionalOnMissingBean(LightningCentralOAuth2JwtTokenCustomizer.class)
    public LightningCentralOAuth2TokenCustomizer<JwtEncodingContext> centralOAuth2JwtTokenCustomizer(
            AuthorizationServerComponentProperties properties
    ) {
        DefaultOpaqueAwareOAuth2TokenCustomizer defaultOpaqueAwareOAuth2TokenCustomizer = new DefaultOpaqueAwareOAuth2TokenCustomizer();
        defaultOpaqueAwareOAuth2TokenCustomizer.setValueTypeFormat(properties.getTokenSettings().getTokenValueFormat());
        return new DelegateCentralOauth2TokenCustomizer<>(
                // 顺序很重要
                new DefaultTokenDetailAwareOAuth2TokenCustomizer(properties.getTokenSettings())::customize,
                defaultOpaqueAwareOAuth2TokenCustomizer::customize
        );
    }

    @Bean
    @ConditionalOnBean(LightningCentralOAuth2JwtTokenCustomizer.class)
    public LightningCentralOAuth2TokenCustomizer<JwtEncodingContext> pluginCentralOAuth2JwtTokenCustomizer(
            AuthorizationServerComponentProperties properties,
            LightningCentralOAuth2JwtTokenCustomizer tokenCustomizer
    ) {
        return new DelegateCentralOauth2TokenCustomizer<>(
                tokenCustomizer,
                centralOAuth2JwtTokenCustomizer(properties)
        );
    }


    /**
     * 支持 oauth2 authorization server configurer ..
     * 并且支持 oauth2 授权服务器的额外配置且扩展,通过{@link LightningOAuth2CentralAuthorizationServerExtConfigurer} 进行配置,
     * 同时它本质上可以通过@Order注解或者Ordered接口进行执行顺序处理,默认是通过{@link org.springframework.core.annotation.AnnotationAwareOrderComparator}
     * 进行顺序比较器处理 顺序,如果没有优先级要求,默认追加到 配置器列表尾部 ...
     * @param extConfigurers 并支持 oauth2 authorization server 扩展配置(例如 支持自定义的授权方式,例如password) ..
     * 详情查看 模块 lightning-central-oauth2-authorization-password-grant-support-server ..
     *
     */
    @Bean
    @SuppressWarnings("unchecked")
    public LightningAppAuthServerConfigurer configurer(
            @Autowired(required = false)
                    List<LightningOAuth2CentralAuthorizationServerExtConfigurer> extConfigurers
    ) {
        return new LightningAppAuthServerConfigurer() {
            @Override
            public void configure(HttpSecurity securityBuilder) throws Exception {

                OAuth2AuthorizationServerConfigurer<HttpSecurity> configurer = securityBuilder.getConfigurer(OAuth2AuthorizationServerConfigurer.class);

                // 启动这个配置 ..
                if (configurer == null) {
                    configurer
                            = OAuth2AuthorizationServerConfigurerExtUtils.getOAuth2AuthorizationServerConfigurer(securityBuilder);
                    securityBuilder.apply(configurer);
                }

                // 增加扩展
                if (!CollectionUtils.isEmpty(extConfigurers)) {
                    for (LightningOAuth2CentralAuthorizationServerExtConfigurer extConfigurer : extConfigurers) {
                        extConfigurer.configure(configurer);
                    }
                }
            }
        };
    }

}
