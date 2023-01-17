package com.generatera.authorization.application.server.config;

import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties.ServerMetaDataEndpointConfig;
import com.generatera.authorization.server.common.configuration.*;
import com.generatera.authorization.server.common.configuration.provider.AuthorizationServerNimbusJwkSetEndpointFilter;
import com.generatera.authorization.server.common.configuration.provider.ProviderExtUtils;
import com.generatera.authorization.server.common.configuration.provider.metadata.AuthorizationProviderContextFilter;
import com.generatera.authorization.server.common.configuration.provider.metadata.AuthorizationServerMetadataEndpointFilter;
import com.generatera.authorization.server.common.configuration.token.DefaultOpaqueAwareTokenCustomizer;
import com.generatera.security.authorization.server.specification.ProviderSettingsProvider;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettings;
import com.generatera.security.authorization.server.specification.components.token.*;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningTokenValueFormat;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.DefaultLightningJwtGenerator;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JWKSourceProvider;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JwtEncodingContext;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.LightningJwtEncoder;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.customizer.LightningJwtCustomizer;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.jose.NimbusJwtEncoder;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import java.net.URI;

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

    private final AuthorizationServerComponentProperties properties;


    // token claims customizer -----------------------------------

    // ------------------- access token  customizer---------------------------------
    @Bean
    @Primary
    @ConditionalOnBean(value = LightningTokenClaimsContext.class, parameterizedContainer = LightningTokenCustomizer.class)
    public LightningTokenCustomizer<LightningTokenClaimsContext> pluginTokenCustomizer(
            AuthorizationServerComponentProperties properties,
            LightningTokenCustomizer<LightningTokenClaimsContext> tokenCustomizer
    ) {
        return new DelegateLightningTokenCustomizer<>(
                tokenCustomizer(),
                tokenCustomizer
        );
    }

    @Bean
    @ConditionalOnMissingBean(value = LightningTokenClaimsContext.class, parameterizedContainer = LightningTokenCustomizer.class)
    public LightningTokenCustomizer<LightningTokenClaimsContext> tokenCustomizer() {
        return new DefaultOpaqueAwareTokenCustomizer(
                LightningTokenValueFormat.OPAQUE.value()
                        .equalsIgnoreCase(properties.getTokenSettings().getTokenValueFormat().value())
        )::customize;
    }

    // -------------------- jwt token customizer --------------------------------------
    @Bean
    @Primary
    @ConditionalOnBean(value = JwtEncodingContext.class, parameterizedContainer = LightningTokenCustomizer.class)
    public LightningTokenCustomizer<JwtEncodingContext> pluginJwtCustomizer(
            LightningTokenCustomizer<JwtEncodingContext> jwtCustomizer
    ) {
        return new DelegateLightningTokenCustomizer<>(
                jwtCustomizer(),
                jwtCustomizer
        );
    }

    @Bean
    @ConditionalOnMissingBean(value = JwtEncodingContext.class, parameterizedContainer = LightningTokenCustomizer.class)
    public LightningTokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return new DefaultOpaqueAwareTokenCustomizer(
                LightningTokenValueFormat.OPAQUE.value()
                        .equalsIgnoreCase(properties.getTokenSettings().getTokenValueFormat().value())
        )::customize;
    }

    // --------------------------- token 生成器 ---------------------------------------
    // 当使用oauth2 central  authorization server的时候,token生成器,根本不会使用到这些Token 生成器 ...
    // 因为 oauth2 是基于 client认证的方式来提供 token
    // 除非需要根据直接使用表单登录进行 用户token 颁发 ...
    // 那么这个token 生成器将可以用于 token 颁发 ...

    // // TODO: 2023/1/11  例如让表单增加token 端点,便于 token 刷新 / token 颁发 ...

    @Bean
    @ConditionalOnBean(LightningJwtEncoder.class)
    @ConditionalOnMissingBean(LightningTokenGenerator.class)
    public LightningTokenGenerator<LightningToken> tokenGenerator(
            LightningJwtEncoder jwtEncoder,
            @Autowired(required = false)
                    LightningJwtCustomizer jwtCustomizer,
            @Autowired(required = false)
                    LightningTokenCustomizer<LightningTokenClaimsContext> tokenClaimsCustomizer) {

        // jwt generator
        DefaultLightningJwtGenerator jwtGenerator = new DefaultLightningJwtGenerator(jwtEncoder);
        ElvisUtil.isNotEmptyConsumer(jwtCustomizer, jwtGenerator::setJwtCustomizer);

        // access generator
        DefaultLightningAccessTokenGenerator accessTokenGenerator = new DefaultLightningAccessTokenGenerator();
        ElvisUtil.isNotEmptyConsumer(tokenClaimsCustomizer, accessTokenGenerator::setAccessTokenCustomizer);

        // opaque token 生成器 判断条件
        return new DelegatingLightningTokenGenerator(
                accessTokenGenerator,
                new DefaultLightningRefreshTokenGenerator(),
                jwtGenerator);
    }


    /**
     * token 名称不能一样,否则当一个bean 方法被跳过时,它也将被跳过 ...
     */
    @Bean
    @ConditionalOnMissingBean({LightningTokenGenerator.class, LightningJwtEncoder.class})
    public LightningTokenGenerator<LightningToken> defaultTokenGenerator(
            JWKSourceProvider jwkSourceProvider,
            @Autowired(required = false)
                    LightningJwtCustomizer jwtCustomizer,
            @Autowired(required = false)
                    LightningTokenCustomizer<LightningTokenClaimsContext> tokenCustomizer
    ) {
        DefaultLightningJwtGenerator jwtGenerator = new DefaultLightningJwtGenerator(new NimbusJwtEncoder(jwkSourceProvider.getJWKSource()));
        ElvisUtil.isNotEmptyConsumer(jwtCustomizer, jwtGenerator::setJwtCustomizer);

        DefaultLightningAccessTokenGenerator accessTokenGenerator = new DefaultLightningAccessTokenGenerator();
        ElvisUtil.isNotEmptyConsumer(tokenCustomizer, accessTokenGenerator::setAccessTokenCustomizer);

        return new DelegatingLightningTokenGenerator(
                accessTokenGenerator,
                new DefaultLightningRefreshTokenGenerator(),
                jwtGenerator
        );
    }
    // --------------------------------------------------------------------------------


    /**
     * oauth2 参考的公共组件 填充处理 ...
     */
    @Bean
    public LightningAppAuthServerConfigurer commonComponentFillConfigurer() {
        return new LightningAppAuthServerConfigurer() {
            @Override
            public void configure(HttpSecurity builder) throws Exception {
                OAuth2AuthorizationServer oAuth2AuthorizationServer = builder.getSharedObject(OAuth2AuthorizationServer.class);
                // 如果不等于 null,则表示oauth2 已经启动 ...
                // 否则填充 一些公共配置(用于 可选的resource server 进一步配置) ..
                if (oAuth2AuthorizationServer == null) {

                    builder.apply(new SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {
                        @Override
                        public void init(HttpSecurity builder) {

                            oauth2CommonComponentFill(builder);
                            oauth2CommonComponentValidation(builder);
                            // oidc provider meta data by AuthServerProviderMetadataConfiguration handle ..
                        }

                        private void oauth2CommonComponentValidation(HttpSecurity builder) {
                            ProviderSettingsProvider providerSettings = ProviderExtUtils.getProviderSettings(builder);
                            validateProviderSettings(providerSettings.getProviderSettings());
                        }


                        private static void validateProviderSettings(ProviderSettings providerSettings) {
                            if (providerSettings.getIssuer() != null) {
                                URI issuerUri;
                                try {
                                    issuerUri = new URI(providerSettings.getIssuer());
                                    issuerUri.toURL();
                                } catch (Exception var3) {
                                    throw new IllegalArgumentException("issuer must be a valid URL", var3);
                                }

                                if (issuerUri.getQuery() != null || issuerUri.getFragment() != null) {
                                    throw new IllegalArgumentException("issuer cannot contain query or fragment component");
                                }
                            }

                        }


                        // oauth2 通用组件复用 ..
                        private void oauth2CommonComponentFill(HttpSecurity builder) {

                            ProviderSettingsProvider providerSettings = ProviderExtUtils.getProviderSettings(builder);
                            AuthorizationProviderContextFilter providerContextFilter
                                    = new AuthorizationProviderContextFilter(providerSettings.getProviderSettings());
                            builder.addFilterAfter(this.postProcess(providerContextFilter), SecurityContextPersistenceFilter.class);
                            JWKSource<SecurityContext> jwkSource = ProviderExtUtils.getJwkSource(builder);

                            // jwk source ..
                            if (jwkSource != null) {
                                AuthorizationServerNimbusJwkSetEndpointFilter jwkSetEndpointFilter
                                        = new AuthorizationServerNimbusJwkSetEndpointFilter(jwkSource,
                                        providerSettings.getProviderSettings().getJwkSetEndpoint());
                                builder.addFilterBefore(
                                        this.postProcess(jwkSetEndpointFilter),
                                        AbstractPreAuthenticatedProcessingFilter.class);
                            }

                            AuthorizationServerMetadataEndpointFilter authorizationServerMetadataEndpointFilter =
                                    new AuthorizationServerMetadataEndpointFilter(providerSettings.getProviderSettings());
                            builder.addFilterBefore(
                                    this.postProcess(authorizationServerMetadataEndpointFilter),
                                    AbstractPreAuthenticatedProcessingFilter.class);
                        }
                    });
                }
            }
        };
    }

    /**
     *
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
