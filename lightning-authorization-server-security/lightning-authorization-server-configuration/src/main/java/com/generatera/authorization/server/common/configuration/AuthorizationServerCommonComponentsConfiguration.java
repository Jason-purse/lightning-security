package com.generatera.authorization.server.common.configuration;

import com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties.StoreKind;
import com.generatera.authorization.server.common.configuration.authorization.LightningAuthorizationService;
import com.generatera.authorization.server.common.configuration.authorization.store.*;
import com.generatera.authorization.server.common.configuration.authorization.store.LightningAuthenticationTokenService.AbstractAuthenticationTokenServiceHandlerProvider;
import com.generatera.authorization.server.common.configuration.util.LogUtil;
import com.generatera.security.authorization.server.specification.HandlerFactory;
import com.generatera.security.authorization.server.specification.LightningUserPrincipalConverter;
import com.generatera.security.authorization.server.specification.TokenSettingsProperties;
import com.generatera.security.authorization.server.specification.TokenSettingsProvider;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JWKSourceProvider;
import com.jianyue.lightning.util.JsonUtil;
import com.nimbusds.jose.jwk.source.JWKSource;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * 授权服务器的 通用组件配置
 * <p>
 * 希望非oauth2 / 或者 oauth2 都遵循 oauth2的一部分规范(token 解析)
 * 例如: 1. token 自解析
 * 2. token 撤销
 * 3. token 自省
 * <p>
 * <p>
 * 当前授权服务器遵循的规范是,都有统一的jwk set
 * 都有统一的 providerSettings
 * 都有统一的 token settings...
 * <p>
 * 但是对于 oauth2 来说(token settings 以 client registration 注册信息为准,如果没有才考虑这个同一个的token settings配置)
 * <p>
 * 作为授权服务器, 需要进行公共的一些组件的注册 ...
 * 此配置文件,配置通用的组件 ..
 * 1. JwkSource
 * 2. Provider settings ...
 * 3. Token settings(这属于颁发应用级别的 token 时常限制)
 * 4. token generator ..
 * 5. authorization service 配置(非oauth2的情况下,我们使用 {@link LightningAuthenticationTokenService}) 否则
 * 使用{@link LightningAuthorizationService}
 */
@Configuration
@AutoConfigureBefore(SecurityAutoConfiguration.class)
@RequiredArgsConstructor
@EnableConfigurationProperties(AuthorizationServerComponentProperties.class)
public class AuthorizationServerCommonComponentsConfiguration implements InitializingBean {

    static {

        // authenticationToken ServiceHandler Providers
        // // TODO: 2023/1/9 加载了额外多的类,没有必要 ...
        // 可以通过 ImportSelector 进行改写,减少类的装载
        HandlerFactory.registerHandler(
                new AbstractAuthenticationTokenServiceHandlerProvider() {

                    @Override
                    public boolean support(Object predicate) {
                        return predicate == StoreKind.MEMORY;
                    }

                    @NotNull
                    @Override
                    public HandlerFactory.Handler getHandler() {
                        return new LightningAuthenticationTokenServiceHandler() {
                            @Override
                            public StoreKind getStoreKind() {
                                return StoreKind.MEMORY;
                            }

                            @Override
                            public LightningAuthenticationTokenService getService(Object... args) {
                                LightningUserPrincipalConverter userPrincipalConverter = (LightningUserPrincipalConverter) args[1];
                                DefaultAuthenticationTokenService authenticationTokenService = new DefaultAuthenticationTokenService();
                                if (userPrincipalConverter != null) {
                                    authenticationTokenService.setTokenConverter(new OptimizedAuthenticationTokenConverter(userPrincipalConverter));
                                }
                                return authenticationTokenService;
                            }
                        };
                    }
                });

        HandlerFactory.registerHandler(
                new AbstractAuthenticationTokenServiceHandlerProvider() {
                    @Override
                    public boolean support(Object predicate) {
                        return predicate == AuthorizationServerComponentProperties.StoreKind.JPA;
                    }

                    @Override
                    public HandlerFactory.Handler getHandler() {
                        return new LightningAuthenticationTokenServiceHandler() {
                            @Override
                            public AuthorizationServerComponentProperties.StoreKind getStoreKind() {
                                return AuthorizationServerComponentProperties.StoreKind.JPA;
                            }

                            @Override
                            public LightningAuthenticationTokenService getService(Object... args) {
                                LightningUserPrincipalConverter userPrincipalConverter = (LightningUserPrincipalConverter) args[1];
                                return new JpaAuthenticationTokenService(userPrincipalConverter);
                            }
                        };
                    }
                });

        HandlerFactory.registerHandler(
                new AbstractAuthenticationTokenServiceHandlerProvider() {
                    @Override
                    public boolean support(Object predicate) {
                        return predicate == AuthorizationServerComponentProperties.StoreKind.MONGO;
                    }

                    @Override
                    public HandlerFactory.Handler getHandler() {
                        return new LightningAuthenticationTokenServiceHandler() {
                            @Override
                            public AuthorizationServerComponentProperties.StoreKind getStoreKind() {
                                return AuthorizationServerComponentProperties.StoreKind.MONGO;
                            }

                            @Override
                            public LightningAuthenticationTokenService getService(Object... args) {
                                LightningUserPrincipalConverter userPrincipalConverter = (LightningUserPrincipalConverter) args[1];
                                return new MongoAuthenticationTokenService(userPrincipalConverter);
                            }
                        };
                    }
                });

        HandlerFactory.registerHandler(
                new AbstractAuthenticationTokenServiceHandlerProvider() {
                    @Override
                    public boolean support(Object predicate) {
                        return predicate == AuthorizationServerComponentProperties.StoreKind.REDIS;
                    }

                    @Override
                    public HandlerFactory.Handler getHandler() {
                        return new LightningAuthenticationTokenServiceHandler() {
                            @Override
                            public AuthorizationServerComponentProperties.StoreKind getStoreKind() {
                                return AuthorizationServerComponentProperties.StoreKind.REDIS;
                            }

                            @Override
                            public LightningAuthenticationTokenService getService(Object... args) {
                                AuthorizationServerComponentProperties properties = (AuthorizationServerComponentProperties) args[0];
                                LightningUserPrincipalConverter userPrincipalConverter = (LightningUserPrincipalConverter) args[1];
                                AuthorizationServerComponentProperties.Redis redis = properties.getAuthorizationStoreConfig().getRedis();
                                return new RedisAuthenticationTokenService(redis.getKeyPrefix(), redis.getExpiredTimeDuration(), userPrincipalConverter);
                            }
                        };
                    }
                });

    }

    private final AuthorizationServerComponentProperties properties;


    /**
     * jwk set(rsa source)
     */
    @Bean
    @ConditionalOnMissingBean(JWKSource.class)
    public JWKSourceProvider jwkSource() {
        return JWKSourceProvider.rsaJWKSourceProvider();
    }

    /**
     * 需要配置SettingProvider
     * <p>
     * ProviderContextHolder 需要单独处理
     */
    @Bean
    public TokenSettingsProvider settingsProvider(JWKSourceProvider jwkSourceProvider) {

        TokenSettingsProperties.Builder builder = TokenSettingsProperties.builder();
        AuthorizationServerComponentProperties.TokenSettings.AccessToken accessToken
                = properties.getTokenSettings().getAccessToken();

        AuthorizationServerComponentProperties.TokenSettings.RefreshToken refreshToken
                = properties.getTokenSettings().getRefreshToken();
        return new TokenSettingsProvider(
                builder
                        .audience(properties.getTokenSettings().getAudiences())
                        // issue format
                        .accessTokenIssueFormat(jwkSourceProvider.getTokenIssueFormat())
                        .accessTokenValueType(accessToken.getTokenValueType())
                        .accessTokenValueFormat(accessToken.getTokenValueFormat())
                        .accessTokenTimeToLive(Duration.ofMillis(accessToken.getTokenTimeToLive()))
                        .refreshTokenValueFormat(refreshToken.getTokenValueFormat())
                        .refreshTokenValueType(refreshToken.getTokenValueType())
                        .refreshTokenTimeToLive(Duration.ofMillis(refreshToken.getTokenTimeToLive()))
                        .reuseRefreshTokens(refreshToken.getReuseRefreshToken())
                        .build()
        );
    }



    /**
     * 需要 authorization service
     * <p>
     * 这是必须的(当不存在的时候)
     * <p>
     * 例如 oauth2 authorization server 是会有自己的 AuthorizationService ..
     */
    @Bean
    @ConditionalOnMissingBean(LightningAuthorizationService.class)
    public LightningAuthenticationTokenService authorizationService(AuthorizationServerComponentProperties properties,
                                                                    @Autowired(required = false)
                                                                            LightningUserPrincipalConverter userPrincipalConverter) {


        HandlerFactory.HandlerProvider provider
                = HandlerFactory.getHandler(LightningAuthenticationTokenService.class,
                Optional.ofNullable(properties.getAuthorizationStoreConfig().getStoreKind()).orElse(StoreKind.MEMORY)
        );
        Assert.notNull(provider, "provider must not be null !!!");
        return ((AbstractAuthenticationTokenServiceHandlerProvider.LightningAuthenticationTokenServiceHandler)
                provider.getHandler())
                .getService(properties, userPrincipalConverter);

    }


    @Bean
    @Primary
    public AuthExtSecurityConfigurer oAuth2ExtSecurityConfigurer(List<LightningAuthServerConfigurer> configurers) {
        return new AuthExtSecurityConfigurer(configurers);
    }



    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain httpSecurity(HttpSecurity httpSecurity,
                                            AuthExtSecurityConfigurer configurer,
                                            @Autowired(required = false)
                                                    List<LightningPermissionConfigurer> permissionConfigurers) throws Exception {
        HttpSecurity builder = httpSecurity
                .apply(configurer)
                .and();


        // permission configuration ..
        if(permissionConfigurers != null) {
            for (LightningPermissionConfigurer permissionConfigurer : permissionConfigurers) {
                AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
                        = builder.authorizeHttpRequests();
                permissionConfigurer.configure(registry);
            }
        }


        return builder
                .apply(permissionHandle())
                .and()
                .build();
    }


    /**
     * 白名单放行(默认处理)
     */
    @NotNull
    private SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> permissionHandle() {
        return new SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {
            @Override
            public void init(HttpSecurity builder) throws Exception {

                // 最后添加这个
                AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry
                        authorizationManagerRequestMatcherRegistry = builder
                        .authorizeHttpRequests();
                if (!CollectionUtils.isEmpty(properties.getPermission().getUrlWhiteList())) {
                    authorizationManagerRequestMatcherRegistry
                            .mvcMatchers(
                                    properties.getPermission().getUrlWhiteList().toArray(String[]::new)
                            )
                            .permitAll();
                }


                authorizationManagerRequestMatcherRegistry
                        .anyRequest()
                        .authenticated()
                        .and()
                        .csrf()
                        .disable();
            }
        };
    }




    @Override
    public void afterPropertiesSet() throws Exception {
        LogUtil.prettyLog("authorization server common component configuration properties print: \n" +
                JsonUtil.getDefaultJsonUtil().asJSON(properties)
        );
    }
}
