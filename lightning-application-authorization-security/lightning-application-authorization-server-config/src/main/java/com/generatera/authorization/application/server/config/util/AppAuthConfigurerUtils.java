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
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.LazyInitTargetSource;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.core.ResolvableType;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.web.authentication.ui.DefaultLogoutPageGeneratingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;

public final class AppAuthConfigurerUtils {

    public static final ContextAnnotationAutowireCandidateResolver resolver = new ContextAnnotationAutowireCandidateResolver();

    private AppAuthConfigurerUtils() {
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
        LightningAuthenticationEntryPoint authenticationEntryPoint = builder.getSharedObject(LightningAuthenticationEntryPoint.class);
        if (authenticationEntryPoint == null) {
            // 尝试从bean 工厂中获取
            LightningAuthenticationEntryPoint bean = getOptionalBean(builder, LightningAuthenticationEntryPoint.class);
            authenticationEntryPoint = bean;
            if (bean == null) {
                ApplicationAuthServerProperties loginProperties = builder.getSharedObject(ApplicationAuthServerProperties.class);
                // todo ...
                DefaultLightningAbstractAuthenticationEntryPoint entryPoint = new DefaultLightningAbstractAuthenticationEntryPoint(
                        loginProperties.getBackendSeparation().getEnableAuthFailureDetails(),
                        loginProperties.getBackendSeparation().getEnableAccountStatusDetails()
                );
                entryPointConfig(entryPoint, loginProperties);

                authenticationEntryPoint = entryPoint;
            }
            builder.setSharedObject(LightningAuthenticationEntryPoint.class, authenticationEntryPoint);
        }

        // 认证entry point
        return authenticationEntryPoint;
    }

    /**
     * 获取 共享对象或者 容器中的 AppAuthServerForTokenAuthenticationProvider ..
     * <p>
     * 如果覆盖,可以设置 share object ..
     * <p>
     * 默认{@link AuthTokenEndpointConfigurer#createDefaultAuthenticationProviders(HttpSecurityBuilder)} 将提供共享对象 ...
     */
    public static <B extends HttpSecurityBuilder<B>> AppAuthServerForTokenAuthenticationProvider getAppAuthServerForTokenAuthenticationProvider(B builder) {
        AppAuthServerForTokenAuthenticationProvider sharedObject = builder.getSharedObject(AuthAccessTokenAuthenticationProvider.class);
        if (sharedObject == null) {
            sharedObject = getOptionalBean(builder, AppAuthServerForTokenAuthenticationProvider.class);
            if (sharedObject == null) {
                LightningAuthenticationTokenService authorizationService = AppAuthConfigurerUtils.getAuthorizationService(builder);
                LightningTokenGenerator<? extends LightningToken> tokenGenerator = AppAuthConfigurerUtils.getTokenGenerator(builder);
                TokenSettingsProvider tokenSettingProvider = AppAuthConfigurerUtils.getTokenSettingProvider(builder);

                sharedObject = new AuthAccessTokenAuthenticationProvider(
                        authorizationService,
                        tokenGenerator,
                        tokenSettingProvider
                );
            }
            builder.setSharedObject(AppAuthServerForTokenAuthenticationProvider.class, sharedObject);
        }

        return sharedObject;
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
        DefaultLogoutPageGeneratingFilter logoutPageGeneratingFilter = builder.getSharedObject(DefaultLogoutPageGeneratingFilter.class);
        if (logoutPageGeneratingFilter == null) {
            // 尝试从bean 工厂中获取
            logoutPageGeneratingFilter = getOptionalBean(builder, DefaultLogoutPageGeneratingFilter.class);
            if (logoutPageGeneratingFilter == null) {
                MyDefaultLogoutPageGeneratingFilter pageGeneratingFilter = new MyDefaultLogoutPageGeneratingFilter();
                logoutPageGeneratingFilter = pageGeneratingFilter;
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
                // 前缀处理 ...
                builder.setSharedObject(DefaultLogoutPageGeneratingFilter.class, logoutPageGeneratingFilter);
            }
        }

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


    public static <B extends HttpSecurityBuilder<B>> LightningTokenGenerator<? extends LightningToken> getTokenGenerator(B builder) {
        LightningTokenGenerator<? extends LightningToken> tokenGenerator = (LightningTokenGenerator<? extends LightningToken>) builder.getSharedObject(LightningTokenGenerator.class);
        TokenSettingsProvider settingsProvider = builder.getSharedObject(TokenSettingsProvider.class);
        if (tokenGenerator == null) {
            tokenGenerator = (LightningTokenGenerator<? extends LightningToken>) getOptionalBean(builder, LightningTokenGenerator.class);
            if (tokenGenerator == null) {
                LightningJwtGenerator jwtGenerator = getJwtGenerator(builder);
                DefaultLightningAccessTokenGenerator accessTokenGenerator = new DefaultLightningAccessTokenGenerator();
                LightningTokenCustomizer<LightningTokenClaimsContext> accessTokenCustomizer = getAccessTokenCustomizer(builder);
                if (accessTokenCustomizer != null) {
                    accessTokenGenerator.setAccessTokenCustomizer(
                            new DelegateLightningTokenCustomizer<>(
                                    accessTokenCustomizer,
                                    new DefaultTokenDetailAwareTokenCustomizer(settingsProvider)::customize,
                                    new DefaultOpaqueAwareTokenCustomizer()::customize
                            ));
                }

                DefaultLightningRefreshTokenGenerator refreshTokenGenerator = new DefaultLightningRefreshTokenGenerator();
                if (jwtGenerator != null) {
                    tokenGenerator = new DelegatingLightningTokenGenerator(jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
                } else {
                    tokenGenerator = new DelegatingLightningTokenGenerator(accessTokenGenerator, refreshTokenGenerator);
                }
            }

            builder.setSharedObject(LightningTokenGenerator.class, tokenGenerator);
        }

        return tokenGenerator;
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

    public static <B extends HttpSecurityBuilder<B>> LightningDaoAuthenticationProvider getDaoAuthenticationProvider(B builder) {
        LightningDaoAuthenticationProvider sharedObject = builder.getSharedObject(LightningDaoAuthenticationProvider.class);
        if (sharedObject == null) {
            Collection<LightningDaoAuthenticationProvider> list = getBeansForType(builder, LightningDaoAuthenticationProvider.class);
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


    public static <B extends HttpSecurityBuilder<B>, T> T getBean(B builder, Class<T> type) {
        return builder.getSharedObject(ApplicationContext.class).getBean(type);
    }

    static <B extends HttpSecurityBuilder<B>, T> T getBean(B builder, ResolvableType type) {
        ApplicationContext context = builder.getSharedObject(ApplicationContext.class);
        String[] names = context.getBeanNamesForType(type);
        if (names.length == 1) {
            return (T) context.getBean(names[0]);
        } else if (names.length > 1) {
            throw new NoUniqueBeanDefinitionException(type, names);
        } else {
            throw new NoSuchBeanDefinitionException(type);
        }
    }

    static <B extends HttpSecurityBuilder<B>, T> T getOptionalBean(B builder, Class<T> type) {
        Map<String, T> beansMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(builder.getSharedObject(ApplicationContext.class), type);
        if (beansMap.size() > 1) {
            int var10003 = beansMap.size();
            String var10004 = type.getName();
            throw new NoUniqueBeanDefinitionException(type, var10003, "Expected single matching bean of type '" + var10004 + "' but found " + beansMap.size() + ": " + StringUtils.collectionToCommaDelimitedString(beansMap.keySet()));
        } else {
            return !beansMap.isEmpty() ? beansMap.values().iterator().next() : null;
        }
    }

    static <B extends HttpSecurityBuilder<B>, T> T getOptionalBean(B builder, ResolvableType type) {
        ApplicationContext context = builder.getSharedObject(ApplicationContext.class);
        String[] names = context.getBeanNamesForType(type);
        if (names.length > 1) {
            throw new NoUniqueBeanDefinitionException(type, names);
        } else {
            return names.length == 1 ? (T) context.getBean(names[0]) : null;
        }
    }

    static <B extends HttpSecurityBuilder<B>, T> Collection<T> getBeansForType(B builder, Class<T> type) {
        ApplicationContext context = builder.getSharedObject(ApplicationContext.class);
        String[] beanNamesForType = context.getBeanNamesForType(type);
        if (beanNamesForType.length > 0) {
            return context.getBeansOfType(type).values();
        }

        return Collections.emptyList();
    }

    static <B extends HttpSecurityBuilder<B>, T> int getBeanNameSizesForType(B builder, Class<T> type) {
        ApplicationContext context = builder.getSharedObject(ApplicationContext.class);
        String[] beanNamesForType = context.getBeanNamesForType(type);
        return beanNamesForType.length;
    }
}
