package com.generatera.authorization.server.common.configuration;

import com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties.StoreKind;
import com.generatera.authorization.server.common.configuration.authorization.LightningAuthorizationService;
import com.generatera.authorization.server.common.configuration.authorization.store.*;
import com.generatera.authorization.server.common.configuration.authorization.store.LightningAuthenticationTokenService.AbstractAuthenticationTokenServiceHandlerProvider;
import com.generatera.authorization.server.common.configuration.util.LogUtil;
import com.generatera.security.authorization.server.specification.HandlerFactory;
import com.generatera.security.authorization.server.specification.ProviderSettingsProvider;
import com.generatera.security.authorization.server.specification.TokenSettings;
import com.generatera.security.authorization.server.specification.TokenSettingsProvider;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettingProperties;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettings;
import com.generatera.security.authorization.server.specification.components.token.*;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.DefaultLightningJwtGenerator;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JWKSourceProvider;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.LightningJwtEncoder;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.customizer.LightningJwtCustomizer;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.jose.NimbusJwtEncoder;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import com.jianyue.lightning.util.JsonUtil;
import com.nimbusds.jose.jwk.source.JWKSource;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.time.Duration;
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
                            public LightningAuthenticationTokenService getService(AuthorizationServerComponentProperties properties) {
                                return new DefaultAuthenticationTokenService();
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
                            public LightningAuthenticationTokenService getService(AuthorizationServerComponentProperties properties) {
                                return new JpaAuthenticationTokenService();
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
                            public LightningAuthenticationTokenService getService(AuthorizationServerComponentProperties properties) {
                                return new MongoAuthenticationTokenService();
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
                            public LightningAuthenticationTokenService getService(AuthorizationServerComponentProperties properties) {
                                AuthorizationServerComponentProperties.Redis redis = properties.getAuthorizationStoreConfig().getRedis();
                                return new RedisAuthenticationTokenService(redis.getKeyPrefix(), redis.getExpiredTimeDuration());
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
     * 需要 authorization service
     * <p>
     * 这是必须的(当不存在的时候)
     * <p>
     * 例如 oauth2 authorization server 是会有自己的 AuthorizationService ..
     */
    @Bean
    @ConditionalOnMissingBean(LightningAuthorizationService.class)
    public LightningAuthenticationTokenService authorizationService(AuthorizationServerComponentProperties properties) {


        HandlerFactory.HandlerProvider provider
                = HandlerFactory.getHandler(LightningAuthenticationTokenService.class,
                Optional.ofNullable(properties.getAuthorizationStoreConfig().getStoreKind()).orElse(StoreKind.MEMORY)
        );
        Assert.notNull(provider, "provider must not be null !!!");
        return ((AbstractAuthenticationTokenServiceHandlerProvider.LightningAuthenticationTokenServiceHandler)
                provider.getHandler())
                .getService(properties);

    }


    /**
     * 需要配置SettingProvider
     * <p>
     * ProviderContextHolder 需要单独处理
     */
    @Bean
    public TokenSettingsProvider settingsProvider(AuthorizationServerComponentProperties properties, JWKSourceProvider jwkSourceProvider) {

        TokenSettings.Builder builder = TokenSettings.builder();

        return new TokenSettingsProvider(
                builder
                        .audience(properties.getTokenSettings().getAudiences())
                        .accessTokenIssueFormat(jwkSourceProvider.getTokenIssueFormat())
                        .accessTokenValueType(properties.getTokenSettings().getTokenValueType())
                        .accessTokenTimeToLive(Duration.ofMillis(properties.getTokenSettings().getAccessTokenTimeToLive()))
                        .refreshTokenIssueFormat(jwkSourceProvider.getTokenIssueFormat())
                        .refreshTokenValueType(properties.getTokenSettings().getTokenValueType())
                        .refreshTokenTimeToLive(Duration.ofMillis(properties.getTokenSettings().getRefreshTokenTimeToLive()))
                        .reuseRefreshTokens(properties.getTokenSettings().getReuseRefreshToken())
                        .build()
        );
    }


    /**
     * 这样做,是为了 统一的 token 生成策略 ...
     * <p>
     * 例如表单也可以遵循 oauth2 的部分规则进行 jwk url 地址查找,从而进一步配置自身 。。
     *
     * @param properties properties ..
     * @return ProviderSettingsProvider
     */
    @Bean
    public ProviderSettingsProvider provider(AuthorizationServerComponentProperties properties) {
        ProviderSettingProperties settingProperties = properties.getProviderSettingProperties();
        final ProviderSettings.Builder builder = ProviderSettings
                .builder();

        // issuer 可以自动生成
        if (StringUtils.isNotBlank(settingProperties.getIssuer())) {
            builder.issuer(settingProperties.getIssuer());
        }
        ProviderSettings settings = builder
                .authorizationEndpoint(settingProperties.getAuthorizationEndpoint())
                .tokenEndpoint(settingProperties.getTokenEndpoint())
                .jwkSetEndpoint(settingProperties.getJwkSetEndpoint())
                .tokenRevocationEndpoint(settingProperties.getTokenRevocationEndpoint())
                .tokenIntrospectionEndpoint(settingProperties.getTokenIntrospectionEndpoint())
                .oidcClientRegistrationEndpoint(settingProperties.getOidcClientRegistrationEndpoint())
                .oidcUserInfoEndpoint(settingProperties.getOidcUserInfoEndpoint())
                .build();

        return new ProviderSettingsProvider(settings);
    }


    // --------------------------- token 生成器 ---------------------------------------
    // 当使用oauth2 central  authorization server的时候,token生成器,根本不会使用到这些Token 生成器 ...
    // 因为 oauth2 是基于 client认证的方式来提供 token
    // 除非需要根据直接使用表单登录进行 用户token 颁发 ...
    // 那么这个token 生成器将可以用于 token 颁发 ...

    // 后续修改为 统一 到 oauth2 的一部分公共规范上
    // // TODO: 2023/1/11  例如让表单增加token 端点,便于 token 刷新 / token 颁发 ...

    @Bean
    @ConditionalOnBean(LightningJwtEncoder.class)
    @ConditionalOnMissingBean(LightningTokenGenerator.class)
    public LightningTokenGenerator<LightningToken> tokenGenerator(
            AuthorizationServerComponentProperties properties,
            LightningJwtEncoder jwtEncoder,
            @Autowired(required = false)
                    LightningJwtCustomizer jwtCustomizer) {
        DefaultLightningJwtGenerator jwtGenerator = new DefaultLightningJwtGenerator(jwtEncoder);
        ElvisUtil.isNotEmptyConsumer(jwtCustomizer, jwtGenerator::setJwtCustomizer);

        // opaque token 生成器 判断条件
        return new DelegatingLightningTokenGenerator(
                new DefaultLightningAccessTokenGenerator(
                        Optional.ofNullable(properties.getTokenSettings().getTokenValueFormat())
                                .map(ele -> ele == LightningTokenType.LightningTokenValueTypeFormat.OPAQUE)
                                .orElse(false)
                ),
                new DefaultLightningRefreshTokenGenerator(),
                jwtGenerator);
    }


    /**
     * token 名称不能一样,否则当一个bean 方法被跳过时,它也将被跳过 ...
     */
    @Bean
    @ConditionalOnMissingBean({LightningTokenGenerator.class, LightningJwtEncoder.class})
    public LightningTokenGenerator<LightningToken> defaultTokenGenerator(
            AuthorizationServerComponentProperties properties,
            JWKSourceProvider jwkSourceProvider,
            @Autowired(required = false)
                    LightningJwtCustomizer jwtCustomizer
    ) {
        DefaultLightningJwtGenerator jwtGenerator = new DefaultLightningJwtGenerator(new NimbusJwtEncoder(jwkSourceProvider.getJWKSource()));
        ElvisUtil.isNotEmptyConsumer(jwtCustomizer, jwtGenerator::setJwtCustomizer);

        return new DelegatingLightningTokenGenerator(
                new DefaultLightningAccessTokenGenerator(
                        Optional.ofNullable(properties.getTokenSettings().getTokenValueFormat())
                                .map(ele -> ele == LightningTokenType.LightningTokenValueTypeFormat.OPAQUE)
                                .orElse(false)
                ),
                new DefaultLightningRefreshTokenGenerator(),
                jwtGenerator
        );
    }
    // --------------------------------------------------------------------------------


    @Override
    public void afterPropertiesSet() throws Exception {
        LogUtil.prettyLog("authorization server common component configuration properties print: \n" +
                JsonUtil.getDefaultJsonUtil().asJSON(properties)
        );
    }
}
