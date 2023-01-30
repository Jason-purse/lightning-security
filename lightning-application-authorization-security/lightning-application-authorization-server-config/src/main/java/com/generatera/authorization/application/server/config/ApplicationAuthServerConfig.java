package com.generatera.authorization.application.server.config;

import com.generatera.authorization.application.server.config.util.AppAuthConfigurerUtils;
import com.generatera.authorization.server.common.configuration.AuthorizationServerCommonComponentsConfiguration;
import com.generatera.authorization.server.common.configuration.LightningAuthServerConfigurer;
import com.generatera.security.authorization.server.specification.DefaultLightningUserDetails;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.ProviderSettingsProvider;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettingProperties;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettings;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
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
                .tokenEndpoint(settingProperties.getTokenEndpoint())
                .jwkSetEndpoint(settingProperties.getJwkSetEndpoint())
                .tokenRevocationEndpoint(settingProperties.getTokenRevocationEndpoint())
                .tokenIntrospectionEndpoint(settingProperties.getTokenIntrospectionEndpoint())
                .build();

        return new ProviderSettingsProvider(settings);
    }


    @Bean
    @Qualifier("userAuthenticationProvider")
    public DaoAuthenticationProvider daoAuthenticationProvider(
            UserDetailsService userDetailsService,
            @Autowired(required = false)
                    PasswordEncoder passwordEncoder,
            @Autowired(required = false)
                    UserDetailsPasswordService passwordManager
    ) {
        UserDetailsService finalUserDetailsService = userDetailsService;
        userDetailsService = username -> {
            UserDetails userDetails = finalUserDetailsService.loadUserByUsername(username);
            if (!LightningUserPrincipal.class.isAssignableFrom(userDetails.getClass())) {
                return new DefaultLightningUserDetails(userDetails);
            }
            return userDetails;
        };

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        if (passwordEncoder != null) {
            provider.setPasswordEncoder(passwordEncoder);
        }

        if (passwordManager != null) {
            provider.setUserDetailsPasswordService(passwordManager);
        }
        return provider;
    }


    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public LightningAuthServerConfigurer bootstrapAppAuthServer(
            @Autowired(required = false) List<LightningAppAuthServerBootstrapConfigurer> appAuthServerConfigurers
    ) {
        return new LightningAuthServerConfigurer() {
            @Override
            public void configure(HttpSecurity security) throws Exception {

                // 分离场景下,才需要增加token 端点进行token 颁发 / 刷新和 撤销 ...
                // 不分离,直接记住我就行 ..(remember me 服务) ...
                // // TODO: 2023/1/30  可以基于 rememberme 标识 来提供刷新token
                if(properties.getIsSeparation()) {
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

                Boolean isSeparation = properties.getIsSeparation();
                ApplicationAuthServerProperties.NoSeparation noSeparation = properties.getNoSeparation();
                String appAuthServerPrefix = org.springframework.util.StringUtils.trimTrailingCharacter(ElvisUtil.stringElvis(properties.getAppAuthPrefix(), AppAuthConfigConstant.APP_AUTH_SERVER_PREFIX), '/');

                if (!isSeparation) {
                    String logoutPageUrl = appAuthServerPrefix + normalize(ElvisUtil.stringElvis(noSeparation.getLogoutPageUrl(), "/logout"));
                    LogoutConfigurer<HttpSecurity> logout = securityBuilder.logout();
                    if (StringUtils.isNotBlank(noSeparation.getLogoutProcessUrl())) {
                        // 设置logout process url
                        String logoutProcessUrl = appAuthServerPrefix + ElvisUtil.stringElvis(noSeparation.getLogoutProcessUrl(), "/logout");
                        logout
                                .logoutRequestMatcher(
                                        new AntPathRequestMatcher(logoutProcessUrl));
                    } else {
                        logout.logoutUrl(logoutPageUrl);
                    }

                    // 登出成功的url
                    logout.logoutSuccessUrl(
                            appAuthServerPrefix + normalize(ElvisUtil.stringElvis(noSeparation.getLogoutSuccessUrl(), "/login?logout"))
                    );

                    // 配置 logout pageGeneratorFilter(由于默认的 限制太深) ...
                    DefaultLogoutPageGeneratingFilter filter = AppAuthConfigurerUtils.getDefaultLogoutPageGeneratingFilter(securityBuilder);
                    // 增加默认登出页面生成过滤器 ..
                    securityBuilder.addFilter(filter);


                    String loginPageUrl = ElvisUtil.stringElvis(noSeparation.getLoginPageUrl(), "/login");
                    loginPageUrl = appAuthServerPrefix + normalize(loginPageUrl);
                    // 可以重新配置 ...
                    String finalLoginPageUrl = loginPageUrl;
                    securityBuilder
                            .exceptionHandling()
                            .authenticationEntryPoint(
                            (request, response, authException) -> response.sendRedirect(finalLoginPageUrl)
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
                // 共享对象存储
                securityBuilder
                        .setSharedObject(ApplicationAuthServerProperties.class, properties);

                // pass,仅仅只是提供这个配置器
                // 应用还可以提供此类LightningAppAuthServerConfigurer 进行进一步配置 ...
                // 放行端点uri
                securityBuilder
                        .authorizeHttpRequests()
                        .requestMatchers(authServerConfigurer.getEndpointsMatcher())
                        .permitAll();
                if (!CollectionUtils.isEmpty(configurers)) {
                    for (LightningAppAuthServerConfigurer configurer : configurers) {
                        configurer.configure(authServerConfigurer);
                    }
                }


            }
        };
    }

    ///**
    // * url 放行
    // * oidc 公共组件 url 放行 ..
    // */
    //@Bean
    //public LightningResourcePermissionConfigurer applicationServerPermissionConfigurer(
    //        ApplicationAuthServerProperties authServerProperties
    //) {
    //    return new LightningResourcePermissionConfigurer() {
    //        @Override
    //        public void configure(AuthorizationManagerRequestMatcherRegistry registry) {
    //            ElvisUtil.isNotEmptyConsumer(
    //                    authServerProperties
    //                            .getServerMetaDataEndpointConfig().getEnableOidc(),
    //                    flag -> registry
    //                            .mvcMatchers(ServerMetaDataEndpointConfig.OPEN_CONNECT_ID_METADATA_ENDPOINT)
    //                            .permitAll());
    //        }
    //    };
    //}


}
