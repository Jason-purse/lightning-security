package com.generatera.authorization.server.common.configuration;

import com.generatera.authorization.server.common.configuration.authorization.LightningAuthorizationService;
import com.generatera.security.authorization.server.specification.BootstrapContext;
import com.generatera.security.authorization.server.specification.HandlerFactory;
import com.generatera.security.authorization.server.specification.TokenSettingsProperties;
import com.generatera.security.authorization.server.specification.TokenSettingsProvider;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JWKSourceProvider;
import com.generatera.security.authorization.server.specification.util.LogUtil;
import com.jianyue.lightning.util.JsonUtil;
import com.nimbusds.jose.jwk.source.JWKSource;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.List;

/**
 * ?????????????????? ??????????????????
 * <p>
 * ?????????oauth2 / ?????? oauth2 ????????? oauth2??????????????????(token ??????)
 * ??????: 1. token ?????????
 * 2. token ??????
 * 3. token ??????
 * <p>
 * <p>
 * ???????????????????????????????????????,???????????????jwk set
 * ??????????????? providerSettings
 * ??????????????? token settings...
 * <p>
 * ???????????? oauth2 ??????(token settings ??? client registration ??????????????????,???????????????????????????????????????token settings??????)
 * <p>
 * ?????????????????????, ?????????????????????????????????????????? ...
 * ???????????????,????????????????????? ..
 * 1. JwkSource
 * 2. Provider settings ...
 * 3. Token settings(?????????????????????????????? token ????????????)
 * 4. token generator ..
 * 5. authorization service ??????(???oauth2????????????,???????????? {@link LightningAuthenticationTokenService}) ??????
 * ??????{@link LightningAuthorizationService}
 */
@Configuration
@AutoConfigureBefore(SecurityAutoConfiguration.class)
@RequiredArgsConstructor
@EnableConfigurationProperties(AuthorizationServerComponentProperties.class)
public class AuthorizationServerCommonComponentsConfiguration implements InitializingBean {


    private final AuthorizationServerComponentProperties properties;


    /**
     * jwk set(rsa source)
     */
    @Bean
    @ConditionalOnMissingBean(JWKSource.class)
    public JWKSourceProvider jwkSource() {
        JwkHandler handler = ((JwkHandler) HandlerFactory.getRequiredHandler(JWKSourceProvider.class, properties.getProviderConfig().getJwkSettings().getCategory()).getHandler());
        return handler.getJwkSourceProvider(properties);
    }

    /**
     * ????????????SettingProvider
     * <p>
     * ProviderContextHolder ??????????????????
     */
    @Bean
    @Qualifier("token_settings_provider")
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
                        .grantTypes(properties.getTokenSettings().getGrantTypes())
                        .build()
        );
    }


    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain httpSecurity(BootstrapContext bootstrapContext,
                                            @Autowired(required = false)
                                                    List<LightningAuthServerConfigurer> configurers,
                                            @Autowired(required = false)
                                                    List<LightningResourcePermissionConfigurer> permissionConfigurers) throws Exception {

        HttpSecurity httpSecurity = bootstrapContext.get(HttpSecurity.class);
        // ??????????????? ...
        assert httpSecurity != null;
        HttpSecurity builder = httpSecurity
                .apply(new AuthExtSecurityConfigurer(configurers))
                .and();

        if (permissionConfigurers != null && !permissionConfigurers.isEmpty()) {
            for (LightningResourcePermissionConfigurer permissionConfigurer : permissionConfigurers) {
                permissionConfigurer.configure(builder.authorizeHttpRequests());
            }
        }


        return builder
                .apply(permissionHandle())
                .and()
                .build();
    }


    /**
     * ???????????????(????????????)
     */
    @NotNull
    private SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> permissionHandle() {
        return new SecurityConfigurerAdapter<>() {
            @Override
            public void init(HttpSecurity builder) throws Exception {

                // ??????????????????
                AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry
                        authorizationManagerRequestMatcherRegistry = builder
                        .authorizeHttpRequests();
                if (!CollectionUtils.isEmpty(properties.getPermission().getUrlWhiteList())) {
                    String[] array = properties.getPermission().getUrlWhiteList().toArray(String[]::new);
                    authorizationManagerRequestMatcherRegistry
                            .antMatchers(array)
                            .permitAll();

                    if (builder.getConfigurer(CsrfConfigurer.class) != null) {
                        authorizationManagerRequestMatcherRegistry.and()
                                .csrf()
                                .ignoringAntMatchers(array);
                    }
                }


                builder.apply(new SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {
                    @Override
                    public void init(HttpSecurity builder) throws Exception {
                        builder.authorizeHttpRequests()
                                .anyRequest()
                                .authenticated();
                    }
                });
            }
        };
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        LogUtil.prettyLog("authorization server common component configuration properties print: \n" +
                JsonUtil.getDefaultJsonUtil().asJSON(properties)
        );
    }

    interface JwkHandlerProvider extends HandlerFactory.HandlerProvider {
        @Override
        default Object key() {
            return JWKSourceProvider.class;
        }
    }

    interface JwkHandler extends HandlerFactory.Handler {
        JWKSourceProvider getJwkSourceProvider(AuthorizationServerComponentProperties properties);
    }

    static {
        // rsa 256
        HandlerFactory.registerHandler(
                new JwkHandlerProvider() {
                    @Override
                    public boolean support(Object predicate) {
                        return predicate == AuthorizationServerComponentProperties.ProviderConfig.JWKCategory.RSA256;
                    }

                    @NotNull
                    @Override
                    public HandlerFactory.Handler getHandler() {
                        return new JwkHandler() {
                            @Override
                            public JWKSourceProvider getJwkSourceProvider(AuthorizationServerComponentProperties properties) {
                                AuthorizationServerComponentProperties.ProviderConfig.RsaJWK rsaJWK = properties.getProviderConfig().getJwkSettings().getRsajwk();
                                Assert.hasText(rsaJWK.getRsaPrivateKey(), "rsa private key must not be null !!!");
                                Assert.hasText(rsaJWK.getRsaPublicKey(), "rsa public key must not be null !!!");
                                AuthorizationServerComponentProperties.ProviderConfig.RsaJWK rsajwk = properties.getProviderConfig().getJwkSettings().getRsajwk();
                                return JWKSourceProvider.customRsaJWKSourceProvider(
                                        rsajwk.getRsaPublicKey(),
                                        rsajwk.getRsaPrivateKey()
                                );
                            }
                        };
                    }
                }
        );
        // secret
        HandlerFactory.registerHandler(
                new JwkHandlerProvider() {
                    @Override
                    public boolean support(Object predicate) {
                        return predicate == AuthorizationServerComponentProperties.ProviderConfig.JWKCategory.SECRET;
                    }

                    @NotNull
                    @Override
                    public HandlerFactory.Handler getHandler() {
                        return new JwkHandler() {
                            @Override
                            public JWKSourceProvider getJwkSourceProvider(AuthorizationServerComponentProperties properties) {
                                AuthorizationServerComponentProperties.ProviderConfig.SecretJWK secretJWK = properties.getProviderConfig().getJwkSettings().getSecretJWK();
                                String key = secretJWK.getKey();
                                Assert.hasText(key, "secret key must not be null !!!");
                                Assert.hasText(secretJWK.getAlgorithm(), "algorithm must not be null !!!");
                                return JWKSourceProvider.customSecretJwkSourceProvider(key, secretJWK.getAlgorithm());
                            }
                        };
                    }
                }
        );

        // fallback
        HandlerFactory.registerHandler(new JwkHandlerProvider() {
            @Override
            public boolean support(Object predicate) {
                return predicate == AuthorizationServerComponentProperties.ProviderConfig.JWKCategory.RANDOM;
            }

            @NotNull
            @Override
            public HandlerFactory.Handler getHandler() {
                return new JwkHandler() {
                    @Override
                    public JWKSourceProvider getJwkSourceProvider(AuthorizationServerComponentProperties properties) {
                        return JWKSourceProvider.rsaJWKSourceProvider();
                    }
                };
            }
        });
    }
}
