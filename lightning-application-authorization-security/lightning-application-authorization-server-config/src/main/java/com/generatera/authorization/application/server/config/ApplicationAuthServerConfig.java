package com.generatera.authorization.application.server.config;

import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties.ServerMetaDataEndpointConfig;
import com.generatera.authorization.server.common.configuration.AuthorizationServerCommonComponentsConfiguration;
import com.generatera.authorization.server.common.configuration.LightningPermissionConfigurer;
import com.generatera.security.authorization.server.specification.ProviderSettingsProvider;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettingProperties;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettings;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry;
import org.springframework.util.Assert;

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


    //// token claims customizer -----------------------------------
    //
    //// ------------------- access token  customizer---------------------------------
    //@Bean
    //@Primary
    //@ConditionalOnBean(value = LightningTokenClaimsContext.class, parameterizedContainer = LightningTokenCustomizer.class)
    //public LightningTokenCustomizer<LightningTokenClaimsContext> pluginTokenCustomizer(
    //        LightningTokenCustomizer<LightningTokenClaimsContext> tokenCustomizer
    //) {
    //    return new DelegateLightningTokenCustomizer<>(
    //            tokenCustomizer,
    //            tokenCustomizer()
    //    );
    //}
    //
    //@Bean
    //@ConditionalOnMissingBean(value = LightningTokenClaimsContext.class, parameterizedContainer = LightningTokenCustomizer.class)
    //public LightningTokenCustomizer<LightningTokenClaimsContext> tokenCustomizer(
    //
    //) {
    //    // access token 生成处理 ..
    //    return new DelegateLightningTokenCustomizer<>(
    //            new DefaultTokenDetailAwareTokenCustomizer(tokenSettingsProvider),
    //            new DefaultOpaqueAwareTokenCustomizer()
    //    )::customize;
    //}
    //
    //// -------------------- jwt token customizer --------------------------------------
    //@Bean
    //@Primary
    //@ConditionalOnBean(value = JwtEncodingContext.class, parameterizedContainer = LightningTokenCustomizer.class)
    //public LightningTokenCustomizer<JwtEncodingContext> pluginJwtCustomizer(
    //        LightningTokenCustomizer<JwtEncodingContext> jwtCustomizer
    //) {
    //    return new DelegateLightningTokenCustomizer<>(
    //            jwtCustomizer,
    //            jwtCustomizer()
    //    );
    //}
    //
    //@Bean
    //@ConditionalOnMissingBean(value = JwtEncodingContext.class, parameterizedContainer = LightningTokenCustomizer.class)
    //public LightningTokenCustomizer<JwtEncodingContext> jwtCustomizer() {
    //    return new DelegateLightningTokenCustomizer<>(
    //            new DefaultTokenDetailAwareTokenCustomizer(tokenSettingsProvider),
    //            new DefaultOpaqueAwareTokenCustomizer()
    //    )::customize;
    //}

    //// --------------------------- token 生成器 ---------------------------------------
    //// 当使用oauth2 central  authorization server的时候,token生成器,根本不会使用到这些Token 生成器 ...
    //// 因为 oauth2 是基于 client认证的方式来提供 token
    //// 除非需要根据直接使用表单登录进行 用户token 颁发 ...
    //// 那么这个token 生成器将可以用于 token 颁发 ...
    //
    //@Bean
    //@ConditionalOnBean(LightningJwtEncoder.class)
    //@ConditionalOnMissingBean(LightningTokenGenerator.class)
    //public LightningTokenGenerator<LightningToken> tokenGenerator(
    //        LightningJwtEncoder jwtEncoder,
    //        @Autowired(required = false)
    //                LightningJwtCustomizer jwtCustomizer,
    //        @Autowired(required = false)
    //                LightningTokenCustomizer<LightningTokenClaimsContext> tokenClaimsCustomizer) {
    //
    //    // jwt generator
    //    DefaultLightningJwtGenerator jwtGenerator = new DefaultLightningJwtGenerator(jwtEncoder);
    //    ElvisUtil.isNotEmptyConsumer(jwtCustomizer, jwtGenerator::setJwtCustomizer);
    //
    //    // access generator
    //    DefaultLightningAccessTokenGenerator accessTokenGenerator = new DefaultLightningAccessTokenGenerator();
    //    ElvisUtil.isNotEmptyConsumer(tokenClaimsCustomizer, accessTokenGenerator::setAccessTokenCustomizer);
    //
    //    // opaque token 生成器 判断条件
    //    return new DelegatingLightningTokenGenerator(
    //            accessTokenGenerator,
    //            new DefaultLightningRefreshTokenGenerator(),
    //            jwtGenerator);
    //}


    ///**
    // * token 名称不能一样,否则当一个bean 方法被跳过时,它也将被跳过 ...
    // */
    //@Bean
    //@ConditionalOnMissingBean({LightningTokenGenerator.class, LightningJwtEncoder.class})
    //public LightningTokenGenerator<LightningToken> defaultTokenGenerator(
    //        JWKSourceProvider jwkSourceProvider,
    //        @Autowired(required = false)
    //                LightningTokenCustomizer<JwtEncodingContext> jwtCustomizer,
    //        @Autowired(required = false)
    //                LightningTokenCustomizer<LightningTokenClaimsContext> tokenCustomizer
    //) {
    //    DefaultLightningJwtGenerator jwtGenerator = new DefaultLightningJwtGenerator(new NimbusJwtEncoder(jwkSourceProvider.getJWKSource()));
    //    ElvisUtil.isNotEmptyConsumer(jwtCustomizer, jwtGenerator::setJwtCustomizer);
    //
    //    DefaultLightningAccessTokenGenerator accessTokenGenerator = new DefaultLightningAccessTokenGenerator();
    //    ElvisUtil.isNotEmptyConsumer(tokenCustomizer, accessTokenGenerator::setAccessTokenCustomizer);
    //
    //    return new DelegatingLightningTokenGenerator(
    //            accessTokenGenerator,
    //            new DefaultLightningRefreshTokenGenerator(),
    //            jwtGenerator
    //    );
    //}
    // --------------------------------------------------------------------------------


    /**
     * 引导 通用组件的配置 ..
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public LightningAppAuthServerConfigurer bootstrapAppAuthServer() {
        return new LightningAppAuthServerConfigurer() {
            @Override
            public void configure(ApplicationAuthServerConfigurer<HttpSecurity> applicationAuthServerConfigurer) throws Exception {
                // pass,仅仅只是提供这个配置器
                // 应用还可以提供此类LightningAppAuthServerConfigurer 进行进一步配置 ...
                // 放行端点uri
                applicationAuthServerConfigurer
                        .and()
                        .authorizeHttpRequests()
                        .requestMatchers(applicationAuthServerConfigurer.getEndpointsMatcher())
                        .permitAll();
            }
        };
    }

    ///**
    // * oauth2 参考的公共组件 填充处理 ...
    // */
    //@Bean
    //public LightningAuthServerConfigurer commonComponentFillConfigurer() {
    //    return new LightningAuthServerConfigurer() {
    //        @Override
    //        public void configure(HttpSecurity builder) throws Exception {
    //            //OAuth2AuthorizationServer oAuth2AuthorizationServer = builder.getSharedObject(OAuth2AuthorizationServer.class);
    //            //// 如果不等于 null,则表示oauth2 已经启动 ...
    //            //// 否则填充 一些公共配置(用于 可选的resource server 进一步配置) ..
    //            //if (oAuth2AuthorizationServer == null) {
    //            //
    //            //    builder.apply(new SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {
    //            //        @Override
    //            //        public void init(HttpSecurity builder) {
    //            //
    //            //            oauth2CommonComponentFill(builder);
    //            //            oauth2CommonComponentValidation(builder);
    //            //            // oidc provider meta data by AuthServerProviderMetadataConfiguration handle ..
    //            //        }
    //            //
    //            //        private void oauth2CommonComponentValidation(HttpSecurity builder) {
    //            //            ProviderSettingsProvider providerSettings = ProviderExtUtils.getProviderSettings(builder);
    //            //            validateProviderSettings(providerSettings.getProviderSettings());
    //            //        }
    //            //
    //            //
    //            //        private static void validateProviderSettings(ProviderSettings providerSettings) {
    //            //            if (providerSettings.getIssuer() != null) {
    //            //                URI issuerUri;
    //            //                try {
    //            //                    issuerUri = new URI(providerSettings.getIssuer());
    //            //                    issuerUri.toURL();
    //            //                } catch (Exception var3) {
    //            //                    throw new IllegalArgumentException("issuer must be a valid URL", var3);
    //            //                }
    //            //
    //            //                if (issuerUri.getQuery() != null || issuerUri.getFragment() != null) {
    //            //                    throw new IllegalArgumentException("issuer cannot contain query or fragment component");
    //            //                }
    //            //            }
    //            //
    //            //        }
    //            //
    //            //
    //            //        // oauth2 通用组件复用 ..
    //            //        private void oauth2CommonComponentFill(HttpSecurity builder) {
    //            //
    //            //            ProviderSettingsProvider providerSettings = ProviderExtUtils.getProviderSettings(builder);
    //            //            AuthorizationProviderContextFilter providerContextFilter
    //            //                    = new AuthorizationProviderContextFilter(providerSettings.getProviderSettings());
    //            //            builder.addFilterAfter(this.postProcess(providerContextFilter), SecurityContextPersistenceFilter.class);
    //            //            JWKSource<SecurityContext> jwkSource = ProviderExtUtils.getJwkSource(builder);
    //            //
    //            //            // jwk source ..
    //            //            if (jwkSource != null) {
    //            //                AuthorizationServerNimbusJwkSetEndpointFilter jwkSetEndpointFilter
    //            //                        = new AuthorizationServerNimbusJwkSetEndpointFilter(jwkSource,
    //            //                        providerSettings.getProviderSettings().getJwkSetEndpoint());
    //            //                builder.addFilterBefore(
    //            //                        this.postProcess(jwkSetEndpointFilter),
    //            //                        AbstractPreAuthenticatedProcessingFilter.class);
    //            //            }
    //            //
    //            //            AuthorizationServerMetadataEndpointFilter authorizationServerMetadataEndpointFilter =
    //            //                    new AuthorizationServerMetadataEndpointFilter(providerSettings.getProviderSettings());
    //            //            builder.addFilterBefore(
    //            //                    this.postProcess(authorizationServerMetadataEndpointFilter),
    //            //                    AbstractPreAuthenticatedProcessingFilter.class);
    //            //        }
    //            //    });
    //            //}
    //        }
    //    };
    //}


    /**
     * url 放行
     * oidc 公共组件 url 放行 ..
     */
    @Bean
    public LightningPermissionConfigurer applicationServerPermissionConfigurer(
            ApplicationAuthServerProperties authServerProperties
    ) {
        return new LightningPermissionConfigurer() {
            @Override
            public void configure(AuthorizationManagerRequestMatcherRegistry registry) {
                ElvisUtil.isNotEmptyConsumer(
                        authServerProperties
                                .getServerMetaDataEndpointConfig().getEnableOidc(),
                        flag -> registry
                                .mvcMatchers(ServerMetaDataEndpointConfig.OPEN_CONNECT_ID_METADATA_ENDPOINT)
                                .permitAll());
            }
        };
    }


}
