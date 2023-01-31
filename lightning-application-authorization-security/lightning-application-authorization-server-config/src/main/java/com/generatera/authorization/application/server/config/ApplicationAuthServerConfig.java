package com.generatera.authorization.application.server.config;

import com.generatera.authorization.application.server.config.util.AppAuthConfigurerUtils;
import com.generatera.authorization.application.server.config.util.ApplicationAuthServerUtils;
import com.generatera.authorization.server.common.configuration.AuthorizationServerCommonComponentsConfiguration;
import com.generatera.authorization.server.common.configuration.LightningAuthServerConfigurer;
import com.generatera.authorization.server.common.configuration.LightningCentralAuthServerConfigurer;
import com.generatera.authorization.server.common.configuration.LightningResourcePermissionConfigurer;
import com.generatera.security.authorization.server.specification.ProviderExtUtils;
import com.generatera.security.authorization.server.specification.ProviderSettingsProvider;
import com.generatera.security.authorization.server.specification.TokenSettingsProperties;
import com.generatera.security.authorization.server.specification.TokenSettingsProvider;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettingProperties;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettings;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.web.authentication.ui.DefaultLogoutPageGeneratingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.generatera.authorization.application.server.config.util.StringUtils.normalize;

/**
 * 此配置作为 整个授权服务器的控制中心(模板配置)
 * <p>
 * 当自定义 AuthExtSecurityConfigurer的情况下,枢纽控制将被破坏,请注意实现 ...
 * 除此之外还处理白名单访问请求路径 ...
 */
@Configuration
@AutoConfiguration
@AutoConfigureAfter(AuthorizationServerCommonComponentsConfiguration.class)
@EnableConfigurationProperties(ApplicationAuthServerProperties.class)
@Import(ApplicationServerImportSelector.class)
@RequiredArgsConstructor
public class ApplicationAuthServerConfig {

    private final ApplicationAuthServerProperties properties;


    /**
     * 这样做,是为了 统一的 token 生成策略 ...
     * <p>
     * 例如表单也可以遵循 oauth2 的部分规则进行 jwk url 地址查找,从而进一步配置自身 。。
     *
     * @return ProviderSettingsProvider
     */
    @Bean
    @Primary
    public ProviderSettingsProvider provider() {
        ProviderSettingProperties settingProperties = properties.getProviderSettingProperties();

        // 直接使用
        ApplicationAuthServerProperties serverProperties = new ApplicationAuthServerUtils(properties)
                .getProperties();
        ProviderSettingProperties providerSettingProperties = serverProperties.getProviderSettingProperties();
        final ProviderSettings.Builder builder = ProviderSettings
                .builder();

        // issuer 可以自动生成
        if (StringUtils.isNotBlank(settingProperties.getIssuer())) {
            builder.issuer(settingProperties.getIssuer());
        }

        // 断言工作
        Assert.notNull(settingProperties.getTokenEndpoint(), "token endpoint must not be null !!!");
        Assert.notNull(settingProperties.getJwkSetEndpoint(), "jwtSet endpoint must not be null !!!");
        Assert.notNull(settingProperties.getTokenIntrospectionEndpoint(), "token introspect endpoint must not be null !!!");
        Assert.notNull(settingProperties.getTokenRevocationEndpoint(), "token revoke endpoint must not be null !!!");

        ProviderSettings settings = builder
                .tokenEndpoint(providerSettingProperties.getTokenEndpoint())
                .jwkSetEndpoint(providerSettingProperties.getJwkSetEndpoint())
                .tokenRevocationEndpoint(providerSettingProperties.getTokenRevocationEndpoint())
                .tokenIntrospectionEndpoint(providerSettingProperties.getTokenIntrospectionEndpoint())
                .build();

        return new ProviderSettingsProvider(settings);
    }


    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public LightningAuthServerConfigurer bootstrapAppAuthServer(
            @Autowired(required = false) List<LightningAppAuthServerBootstrapConfigurer> appAuthServerConfigurers
    ) {
        return new LightningAuthServerConfigurer() {
            @Override
            public void configure(HttpSecurity security) throws Exception {

                // 应用 授权服务器 ..utils
                ApplicationAuthServerUtils applicationAuthServerUtils = new ApplicationAuthServerUtils(properties);
                security.setSharedObject(ApplicationAuthServerUtils.class,applicationAuthServerUtils);

                // 分离场景下,才需要增加token 端点进行token 颁发 / 刷新和 撤销 ...
                // 不分离,直接记住我就行 ..(remember me 服务) ...
                // // TODO: 2023/1/30  可以基于 rememberme 标识 来提供刷新token
                ApplicationAuthServerProperties serverProperties = security.getSharedObject(ApplicationAuthServerProperties.class);
                if (serverProperties == null) {
                    security.setSharedObject(ApplicationAuthServerProperties.class, properties);
                }

                // 配置 普通应用级别的 授权服务器 ..
                if (appAuthServerConfigurers != null && !appAuthServerConfigurers.isEmpty()) {
                    for (LightningAppAuthServerBootstrapConfigurer appAuthServerConfigurer : appAuthServerConfigurers) {
                        appAuthServerConfigurer.configure(security);
                    }
                }
            }
        };
    }


    /**
     * 非分离下的 登录或者登出页面配置 ...
     */
    @Bean
    public LightningAppAuthServerBootstrapConfigurer loginOrLogoutPageConfig() {
        return new LightningAppAuthServerBootstrapConfigurer() {
            @Override
            public void configure(HttpSecurity securityBuilder) throws Exception {
                // 必须认证 ...
                securityBuilder
                        // 保留 csrf 提供的默认页面功能 ...
                        .csrf()
                        // 但是忽略所有请求
                        .ignoringAntMatchers("/**");

                boolean isSeparation = properties.isSeparation();
                ApplicationAuthServerUtils applicationAuthServerPropertiesUtils = ApplicationAuthServerUtils.getApplicationAuthServerProperties(securityBuilder);
                ApplicationAuthServerProperties.NoSeparation noSeparation = applicationAuthServerPropertiesUtils.getProperties().getNoSeparation();

                if (!isSeparation) {
                    String logoutPageUrl = noSeparation.getLogoutPageUrl();
                    LogoutConfigurer<HttpSecurity> logout = securityBuilder.logout();
                    if (StringUtils.isNotBlank(noSeparation.getLogoutProcessUrl())) {
                        // 设置logout process url
                        String logoutProcessUrl = noSeparation.getLogoutProcessUrl();
                        logout
                                .logoutRequestMatcher(
                                        new AntPathRequestMatcher(logoutProcessUrl));
                    } else {
                        logout.logoutUrl(logoutPageUrl);
                    }

                    // 登出成功的url
                    logout.logoutSuccessUrl(noSeparation.getLogoutSuccessUrl());

                    // 配置 logout pageGeneratorFilter(由于默认的 限制太深) ...
                    DefaultLogoutPageGeneratingFilter filter = AppAuthConfigurerUtils.getDefaultLogoutPageGeneratingFilter(securityBuilder);
                    // 增加默认登出页面生成过滤器 ..
                    securityBuilder.addFilter(filter);


                    String loginPageUrl = noSeparation.getLogoutPageUrl();
                    // 可以重新配置 ...
                    securityBuilder
                            .exceptionHandling()
                            .authenticationEntryPoint(
                                    (request, response, authException) -> response.sendRedirect(loginPageUrl)
                            );
                }
            }
        };
    }


    /**
     * 引导token 相关端点的配置处理  ...
     */
    @Bean
    public LightningAppAuthServerBootstrapConfigurer tokenEndpointsConfig(
            @Autowired(required = false)
                    List<LightningAppAuthServerConfigurer> configurers
    ) throws Exception {
        return new LightningAppAuthServerBootstrapConfigurer() {
            @Override
            public void configure(HttpSecurity securityBuilder) throws Exception {
                ApplicationAuthServerConfigurer<HttpSecurity> authServerConfigurer = new ApplicationAuthServerConfigurer<>();
                securityBuilder.apply(authServerConfigurer);
                // 设置为共享对象 ..
                securityBuilder.setSharedObject(ApplicationAuthServerConfigurer.class, authServerConfigurer);

                if (!CollectionUtils.isEmpty(configurers)) {
                    for (LightningAppAuthServerConfigurer configurer : configurers) {
                        configurer.configure(authServerConfigurer);
                    }
                }

                // pass,仅仅只是提供这个配置器
                // 应用还可以提供此类LightningAppAuthServerConfigurer 进行进一步配置 ...
                // 放行端点uri
                securityBuilder
                        .authorizeHttpRequests()
                        .requestMatchers(authServerConfigurer.getEndpointsMatcher())
                        .permitAll();

            }
        };
    }

    /**
     * 如果是opaque token ,那么将启用token 省查端点 ...
     */
    @Bean
    public LightningAuthServerConfigurer opaqueTokenConfigurer(TokenSettingsProvider tokenSettingsProvider) {
        return new LightningAuthServerConfigurer() {
            @Override
            public void configure(HttpSecurity securityBuilder) throws Exception {
                TokenSettingsProperties tokenSettings = tokenSettingsProvider.getTokenSettings();
                ApplicationAuthServerConfigurer<HttpSecurity> configurer = (ApplicationAuthServerConfigurer<HttpSecurity>) securityBuilder.getConfigurer(ApplicationAuthServerConfigurer.class);
                LightningTokenType.LightningTokenValueFormat accessTokenValueFormat = tokenSettings.getAccessTokenValueFormat();
                if (accessTokenValueFormat == LightningTokenType.LightningTokenValueFormat.OPAQUE) {
                    configurer.tokenIntrospectionEndpoint(Customizer.withDefaults());
                }
            }
        };
    }

    /**
     * url 放行
     * oidc 公共组件 url 放行 ..
     * {@link ApplicationServerImportSelector#selectImports(AnnotationMetadata)}
     */
    @Bean
    public LightningResourcePermissionConfigurer applicationServerPermissionConfigurer(
            ApplicationAuthServerProperties authServerProperties,
            @Autowired(required = false)
                    List<LightningCentralAuthServerConfigurer> configurers
    ) {
        return new LightningResourcePermissionConfigurer() {
            @Override
            public void configure(AuthorizationManagerRequestMatcherRegistry registry) {
                String openIdConnectMetaData = ElvisUtil.stringElvis(authServerProperties.getServerMetaDataEndpointConfig().getOpenConnectIdMetadataEndpointUri(), ApplicationAuthServerProperties.ServerMetaDataEndpointConfig.OPEN_CONNECT_ID_METADATA_ENDPOINT);

                String appAuthPrefix = ElvisUtil.stringElvis(authServerProperties.appAuthPrefix, AppAuthConfigConstant.APP_AUTH_SERVER_PREFIX);

                // 存在 中央授权服务器 ...
                if (configurers != null && configurers.size() > 0) {
                    openIdConnectMetaData = appAuthPrefix + normalize(openIdConnectMetaData);

                    ProviderSettingsProvider providerSettings = ProviderExtUtils.getProviderSettings(registry.and());
                    // 增加前缀 ..
                    // TODO
                    providerSettings.getProviderSettings();
                }
                boolean enableOidc = authServerProperties
                        .getServerMetaDataEndpointConfig().isEnableOidc();

                if (enableOidc) {
                    registry
                            .mvcMatchers(openIdConnectMetaData)
                            .permitAll();
                }
            }
        };
    }

}
