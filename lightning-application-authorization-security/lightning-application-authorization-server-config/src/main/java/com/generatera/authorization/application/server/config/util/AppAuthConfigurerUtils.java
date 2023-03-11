package com.generatera.authorization.application.server.config.util;

import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
import com.generatera.authorization.application.server.config.MyDefaultLogoutPageGeneratingFilter;
import com.generatera.authorization.application.server.config.authentication.DefaultLightningAbstractAuthenticationEntryPoint;
import com.generatera.authorization.application.server.config.authorization.store.LightningAuthenticationTokenService;
import com.generatera.authorization.application.server.config.token.*;
import com.generatera.authorization.server.common.configuration.LightningCentralAuthServer;
import com.generatera.security.authorization.server.specification.AuthServerProvider;
import com.generatera.security.authorization.server.specification.ProviderExtUtils;
import com.generatera.security.authorization.server.specification.TokenSettingsProvider;
import com.generatera.security.authorization.server.specification.components.authentication.LightningAuthenticationEntryPoint;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettingProperties;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettings;
import com.generatera.security.authorization.server.specification.components.token.*;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.DefaultLightningJwtGenerator;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JwtEncodingContext;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.LightningJwtEncoder;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.LightningJwtGenerator;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.jose.NimbusJwtEncoder;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.LazyInitTargetSource;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.web.authentication.ui.DefaultLogoutPageGeneratingFilter;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static com.generatera.security.authorization.server.specification.util.HttpSecurityBuilderUtils.*;

public final class AppAuthConfigurerUtils {

    public static final ContextAnnotationAutowireCandidateResolver resolver = new ContextAnnotationAutowireCandidateResolver();

    private AppAuthConfigurerUtils() {
    }

    //// TODO: 2023/2/2  代码没有写完
    @Deprecated
    public static <B extends HttpSecurityBuilder<B>> SecurityContextRepository getSecurityContextRepository(B builder) {
        return getBean(builder, SecurityContextRepository.class, new Function<B, SecurityContextRepository>() {
            @Override
            public SecurityContextRepository apply(B b) {
                HttpSessionSecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();
                ApplicationAuthServerUtils authServerUtils = ApplicationAuthServerUtils.getApplicationAuthServerProperties(b);
                return new SecurityContextRepository() {
                    @Override
                    public org.springframework.security.core.context.SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
                        return contextRepository.loadContext(requestResponseHolder);
                    }

                    @Override
                    public void saveContext(org.springframework.security.core.context.SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
                        Cookie[] cookies = request.getCookies();

                    }

                    @Override
                    public boolean containsContext(HttpServletRequest request) {
                        return false;
                    }
                };
            }
        });
    }

    public static <B extends HttpSecurityBuilder<B>> AuthServerProvider getProviderSettings(B builder) {
        AuthServerProvider provider = builder.getSharedObject(AuthServerProvider.class);
        if (provider == null) {
            provider = getOptionalBean(builder, AuthServerProvider.class);
            if (provider == null) {
                ApplicationAuthServerUtils applicationAuthServerUtils = ApplicationAuthServerUtils.getApplicationAuthServerProperties(builder);
                // 是否存在 auth Server ...
                LightningCentralAuthServer authServer = builder.getSharedObject(LightningCentralAuthServer.class);
                provider = providerSettings(authServer != null ? applicationAuthServerUtils.getFullConfigProperties() : applicationAuthServerUtils.getProperties());
                builder.setSharedObject(AuthServerProvider.class, provider);
            }
        }

        return provider;
    }

    private static ProviderSettings providerSettings(ApplicationAuthServerProperties authServerProperties) {
        ProviderSettingProperties settingProperties = authServerProperties.getProviderSettingProperties();


        final ProviderSettings.Builder builder = ProviderSettings
                .builder();

        // issuer 可以自动生成
        if (org.apache.commons.lang3.StringUtils.isNotBlank(settingProperties.getIssuer())) {
            builder.issuer(settingProperties.getIssuer());
        }

        return builder
                .tokenEndpoint(settingProperties.getTokenEndpoint())
                .jwkSetEndpoint(settingProperties.getJwkSetEndpoint())
                .tokenRevocationEndpoint(settingProperties.getTokenRevocationEndpoint())
                .tokenIntrospectionEndpoint(settingProperties.getTokenIntrospectionEndpoint())
                .build();
    }

    public static <B extends HttpSecurityBuilder<B>> LightningAuthenticationEntryPoint getAuthenticationEntryPoint(B builder) {
        return getBean(builder, LightningAuthenticationEntryPoint.class, () -> {
            ApplicationAuthServerProperties loginProperties = builder.getSharedObject(ApplicationAuthServerProperties.class);
            // todo ...
            DefaultLightningAbstractAuthenticationEntryPoint entryPoint = new DefaultLightningAbstractAuthenticationEntryPoint(
                    loginProperties.getBackendSeparation().getEnableAuthFailureDetails(),
                    loginProperties.getBackendSeparation().getEnableAccountStatusDetails()
            );
            entryPointConfig(entryPoint, loginProperties);
            return entryPoint;
        });
    }

    /**
     * 获取 共享对象或者 容器中的 AppAuthServerForTokenAuthenticationProvider ..
     * <p>
     * 如果覆盖,可以设置 share object ..
     * <p>
     * 默认{@link AuthTokenEndpointConfigurer#createDefaultAuthenticationProviders(HttpSecurityBuilder)} 将提供共享对象 ...
     */
    public static <B extends HttpSecurityBuilder<B>> AppAuthServerForTokenAuthenticationProvider getAppAuthServerForTokenAuthenticationProvider(B builder) {
        return getBean(builder, AppAuthServerForTokenAuthenticationProvider.class, () -> {
            LightningAuthenticationTokenService authorizationService = AppAuthConfigurerUtils.getAuthorizationService(builder);
            LightningTokenGenerator<? extends LightningToken> tokenGenerator = AppAuthConfigurerUtils.getTokenGenerator(builder);
            TokenSettingsProvider tokenSettingProvider = AppAuthConfigurerUtils.getTokenSettingProvider(builder);
            return new AuthAccessTokenAuthenticationProvider(
                    authorizationService,
                    tokenGenerator,
                    tokenSettingProvider
            );
        });
    }

    private static void entryPointConfig(DefaultLightningAbstractAuthenticationEntryPoint point, ApplicationAuthServerProperties properties) {
        ApplicationAuthServerProperties.BackendSeparation backendSeparation = properties.getBackendSeparation();

        if (StringUtils.hasText(backendSeparation.getLoginSuccessMessage())) {
            point.setLoginSuccessMessage(backendSeparation.getLoginSuccessMessage());
        }

        if (ObjectUtils.isNotEmpty(backendSeparation.getEnableAccountStatusDetails()) && backendSeparation.getEnableAccountStatusDetails()) {
            if (StringUtils.hasText(backendSeparation.getAccountLockedMessage())) {
                point.setAccountLockedMessage(backendSeparation.getAccountLockedMessage());
            }
            if (StringUtils.hasText(backendSeparation.getAccountExpiredMessage())) {
                point.setAccountExpiredMessage(backendSeparation.getAccountExpiredMessage());
            }
        }

        if (StringUtils.hasText(backendSeparation.getAccountStatusMessage())) {
            point.setAccountStatusMessage(backendSeparation.getAccountStatusMessage());
        }

        if (StringUtils.hasText(backendSeparation.getBadCredentialMessage())) {
            point.setBadCredentialsMessage(backendSeparation.getBadCredentialMessage());
        }

        if (StringUtils.hasText(backendSeparation.getLoginFailureMessage())) {
            point.setLoginFailureMessage(backendSeparation.getLoginFailureMessage());
        }

        if (StringUtils.hasText(backendSeparation.getUnAuthenticatedMessage())) {
            point.setUnAuthenticatedMessage(backendSeparation.getUnAuthenticatedMessage());
        }
    }

    public static <B extends HttpSecurityBuilder<B>> DefaultLogoutPageGeneratingFilter configDefaultLogoutPageGeneratingFilter(B builder) {
        DefaultLogoutPageGeneratingFilter logoutPageGeneratingFilter =
                getBean(builder, DefaultLogoutPageGeneratingFilter.class, () -> {
                    MyDefaultLogoutPageGeneratingFilter pageGeneratingFilter = new MyDefaultLogoutPageGeneratingFilter();
                    ApplicationAuthServerUtils applicationAuthServerUtils = ApplicationAuthServerUtils.getApplicationAuthServerProperties(builder);
                    ApplicationAuthServerProperties.NoSeparation noSeparation = applicationAuthServerUtils.getProperties().getNoSeparation();

                    // 登出页面配置,
                    // 登出处理路径配置
                    // 使用自己的 登出页面生成过滤器 ..

                    // 配置 logoutprocess url (用于渲染对应的页面) ..
                    pageGeneratingFilter.setMatcher(new AntPathRequestMatcher(noSeparation.getLogoutPageUrl(), "GET"));
                    if (StringUtils.hasText(noSeparation.getLogoutProcessUrl())) {
                        String logoutProcessUrl = noSeparation.getLogoutProcessUrl();
                        pageGeneratingFilter.setLogoutProcessUrl(logoutProcessUrl);
                    } else {
                        pageGeneratingFilter.setLogoutProcessUrl(noSeparation.getLogoutPageUrl());
                    }
                    return pageGeneratingFilter;
                });

        if (builder.getSharedObject(MyDefaultLogoutPageGeneratingFilter.class) == null) {
            // 用于标识,已经注册了logoutPageGeneratingFilter ..
            builder.setSharedObject(MyDefaultLogoutPageGeneratingFilter.class, new MyDefaultLogoutPageGeneratingFilter());
            builder.addFilter(logoutPageGeneratingFilter);
        }

        return logoutPageGeneratingFilter;
    }

    public static <B extends HttpSecurityBuilder<B>> LightningAuthenticationTokenService getAuthorizationService(B builder) {
        LightningAuthenticationTokenService authorizationService = builder.getSharedObject(LightningAuthenticationTokenService.class);
        if (authorizationService == null) {
            authorizationService = getBean(builder, LightningAuthenticationTokenService.class);
            builder.setSharedObject(LightningAuthenticationTokenService.class, authorizationService);
        }

        return authorizationService;
    }

    @SuppressWarnings("unchecked")
    public static <B extends HttpSecurityBuilder<B>> LightningTokenGenerator<? extends LightningToken> getTokenGenerator(B builder) {
        return getBean(builder, LightningTokenGenerator.class, () -> {
            LightningJwtGenerator jwtGenerator = getJwtGenerator(builder);
            DefaultLightningAccessTokenGenerator accessTokenGenerator = getAccessTokenGenerator(builder);
            DefaultLightningRefreshTokenGenerator refreshTokenGenerator = new DefaultLightningRefreshTokenGenerator();
            if (jwtGenerator != null) {
                return new DelegatingLightningTokenGenerator(jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
            } else {
                return new DelegatingLightningTokenGenerator(accessTokenGenerator, refreshTokenGenerator);
            }
        });
    }

    @NotNull
    private static <B extends HttpSecurityBuilder<B>> DefaultLightningAccessTokenGenerator getAccessTokenGenerator(B builder) {
        DefaultLightningAccessTokenGenerator accessTokenGenerator = new DefaultLightningAccessTokenGenerator();
        LightningTokenCustomizer<LightningTokenClaimsContext> accessTokenCustomizer = getAccessTokenCustomizer(builder);
        TokenSettingsProvider settingsProvider = builder.getSharedObject(TokenSettingsProvider.class);
        if (accessTokenCustomizer != null) {
            accessTokenGenerator.setAccessTokenCustomizer(
                    new DelegateLightningTokenCustomizer<>(
                            accessTokenCustomizer,
                            new DefaultTokenDetailAwareTokenCustomizer(settingsProvider)::customize,
                            new DefaultOpaqueAwareTokenCustomizer()::customize
                    ));
        }
        else {
            accessTokenGenerator.setAccessTokenCustomizer(
                    new DelegateLightningTokenCustomizer<>(
                            new DefaultTokenDetailAwareTokenCustomizer(settingsProvider)::customize,
                            new DefaultOpaqueAwareTokenCustomizer()::customize
                    )
            );
        }
        return accessTokenGenerator;
    }

    private static <B extends HttpSecurityBuilder<B>> LightningJwtGenerator getJwtGenerator(B builder) {
        LightningJwtGenerator jwtGenerator = builder.getSharedObject(LightningJwtGenerator.class);
        if (jwtGenerator == null) {
            LightningJwtEncoder jwtEncoder = getJwtEncoder(builder);
            if (jwtEncoder != null) {
                DefaultLightningJwtGenerator defaultLightningJwtGenerator = new DefaultLightningJwtGenerator(jwtEncoder);
                jwtGenerator = defaultLightningJwtGenerator;
                LightningTokenCustomizer<JwtEncodingContext> jwtCustomizer = getJwtCustomizer(builder);
                if (jwtCustomizer != null) {
                    defaultLightningJwtGenerator.setJwtCustomizer(
                            new DelegateLightningTokenCustomizer<>(
                                    jwtCustomizer,
                                    new DefaultTokenDetailAwareTokenCustomizer(getTokenSettingProvider(builder))::customize,
                                    new DefaultOpaqueAwareTokenCustomizer()::customize
                            )
                    );
                }
                else {
                    defaultLightningJwtGenerator.setJwtCustomizer(
                            new DelegateLightningTokenCustomizer<>(
                                    new DefaultTokenDetailAwareTokenCustomizer(getTokenSettingProvider(builder))::customize,
                                    new DefaultOpaqueAwareTokenCustomizer()::customize
                            )
                    );
                }

                builder.setSharedObject(LightningJwtGenerator.class, jwtGenerator);
            }
        }

        return jwtGenerator;
    }

    private static <B extends HttpSecurityBuilder<B>> LightningJwtEncoder getJwtEncoder(B builder) {
        LightningJwtEncoder jwtEncoder = builder.getSharedObject(LightningJwtEncoder.class);
        if (jwtEncoder == null) {
            jwtEncoder = getOptionalBean(builder, LightningJwtEncoder.class);
            if (jwtEncoder == null) {
                JWKSource<SecurityContext> jwkSource = getJwkSource(builder);
                if (jwkSource != null) {
                    jwtEncoder = new NimbusJwtEncoder(jwkSource);
                }
            }

            if (jwtEncoder != null) {
                builder.setSharedObject(LightningJwtEncoder.class, jwtEncoder);
            }
        }

        return jwtEncoder;
    }

    @SuppressWarnings("unchecked")
    static <B extends HttpSecurityBuilder<B>> JWKSource<SecurityContext> getJwkSource(B builder) {
        return ProviderExtUtils.getJwkSource(builder);
    }

    private static <B extends HttpSecurityBuilder<B>> LightningTokenCustomizer<JwtEncodingContext> getJwtCustomizer(B builder) {
        ResolvableType type = ResolvableType.forClassWithGenerics(LightningTokenCustomizer.class, JwtEncodingContext.class);
        return getOptionalBean(builder, type);
    }

    private static <B extends HttpSecurityBuilder<B>> LightningTokenCustomizer<LightningTokenClaimsContext> getAccessTokenCustomizer(B builder) {
        ResolvableType type = ResolvableType.forClassWithGenerics(LightningTokenCustomizer.class, LightningTokenClaimsContext.class);
        return getOptionalBean(builder, type);
    }

    public static <B extends HttpSecurityBuilder<B>> TokenSettingsProvider getTokenSettingProvider(B builder) {
        TokenSettingsProvider sharedObject = builder.getSharedObject(TokenSettingsProvider.class);
        if (sharedObject == null) {
            ResolvableType type = ResolvableType.forClass(TokenSettingsProvider.class);
            sharedObject = getBean(builder, type);
            builder.setSharedObject(TokenSettingsProvider.class, sharedObject);
        }
        return sharedObject;
    }


    public static <B extends HttpSecurityBuilder<B>> LightningUserDetailsProvider getLightningUserDetailsProvider(B builder) {
        LightningUserDetailsProvider sharedObject = builder.getSharedObject(LightningUserDetailsProvider.class);
        if (sharedObject == null) {
            Collection<LightningUserDetailsProvider> beans = getBeansForType(builder, LightningUserDetailsProvider.class);
            DelegateLightningUserDetailsProvider delegateLightningUserDetailsProvider = new DelegateLightningUserDetailsProvider(beans);
            builder.setSharedObject(LightningUserDetailsProvider.class, delegateLightningUserDetailsProvider);
            sharedObject = delegateLightningUserDetailsProvider;
        }
        return sharedObject;
    }

    /**
     * 最好不要覆盖 共享对象 LightningDaoAuthenticationProvider
     * 否则,你可能需要自己 写入这下面的一段逻辑,来保证 dao 认证提供器代理 ...
     */
    public static <B extends HttpSecurityBuilder<B>> LightningDaoAuthenticationProvider getDaoAuthenticationProvider(B builder,List<LightningDaoAuthenticationProvider> providers) {
        LightningDaoAuthenticationProvider sharedObject = builder.getSharedObject(LightningDaoAuthenticationProvider.class);
        if (sharedObject == null) {
            Collection<LightningDaoAuthenticationProvider> list = getBeansForType(builder, LightningDaoAuthenticationProvider.class);
            providers.addAll(list);
            list = providers.stream().sorted(AnnotationAwareOrderComparator.INSTANCE).toList();
            DelegateLightningDaoAuthenticationProvider daoAuthenticationProvider = new DelegateLightningDaoAuthenticationProvider(list);
            builder.setSharedObject(LightningDaoAuthenticationProvider.class, daoAuthenticationProvider);
            sharedObject = daoAuthenticationProvider;
        }
        return sharedObject;
    }


    private static <T> T lazyBean(ApplicationContext context, ObjectPostProcessor<Object> objectPostProcessor, Class<T> interfaceName) {
        LazyInitTargetSource lazyTargetSource = new LazyInitTargetSource();
        String[] beanNamesForType = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context, interfaceName);
        if (beanNamesForType.length == 0) {
            return null;
        } else {
            String beanName = getBeanName(interfaceName, context, beanNamesForType);
            lazyTargetSource.setTargetBeanName(beanName);
            lazyTargetSource.setBeanFactory(context);
            ProxyFactoryBean proxyFactory = new ProxyFactoryBean();
            proxyFactory = objectPostProcessor.postProcess(proxyFactory);
            proxyFactory.setTargetSource(lazyTargetSource);
            return (T) proxyFactory.getObject();
        }
    }

    private static <T> String getBeanName(Class<T> interfaceName, ApplicationContext applicationContext, String[] beanNamesForType) {
        if (beanNamesForType.length == 1) {
            return beanNamesForType[0];
        } else {
            List<String> primaryBeanNames = getPrimaryBeanNames(applicationContext, beanNamesForType);
            Assert.isTrue(primaryBeanNames.size() != 0, () -> {
                return "Found " + beanNamesForType.length + " beans for type " + interfaceName + ", but none marked as primary";
            });
            Assert.isTrue(primaryBeanNames.size() == 1, () -> {
                return "Found " + primaryBeanNames.size() + " beans for type " + interfaceName + " marked as primary";
            });
            return (String) primaryBeanNames.get(0);
        }
    }

    private static List<String> getPrimaryBeanNames(ApplicationContext applicationContext, String[] beanNamesForType) {
        List<String> list = new ArrayList<>();
        if (!(applicationContext instanceof ConfigurableApplicationContext)) {
            return Collections.emptyList();
        } else {
            String[] var3 = beanNamesForType;
            int var4 = beanNamesForType.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                String beanName = var3[var5];
                if (((ConfigurableApplicationContext) applicationContext).getBeanFactory().getBeanDefinition(beanName).isPrimary()) {
                    list.add(beanName);
                }
            }

            return list;
        }
    }








}
