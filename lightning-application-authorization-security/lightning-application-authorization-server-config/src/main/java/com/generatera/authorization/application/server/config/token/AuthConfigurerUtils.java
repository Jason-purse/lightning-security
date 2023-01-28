package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.server.common.configuration.authorization.store.DefaultAuthenticationTokenService;
import com.generatera.authorization.server.common.configuration.authorization.store.LightningAuthenticationTokenService;
import com.generatera.security.authorization.server.specification.ProviderExtUtils;
import com.generatera.security.authorization.server.specification.TokenSettingsProvider;
import com.generatera.security.authorization.server.specification.components.token.*;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.DefaultLightningJwtGenerator;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JwtEncodingContext;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.LightningJwtEncoder;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.LightningJwtGenerator;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.jose.NimbusJwtEncoder;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.util.StringUtils;

import java.util.Map;

final class AuthConfigurerUtils {
    private AuthConfigurerUtils() {
    }

    static <B extends HttpSecurityBuilder<B>> LightningAuthenticationTokenService getAuthorizationService(B builder) {
        LightningAuthenticationTokenService authorizationService = builder.getSharedObject(LightningAuthenticationTokenService.class);
        if (authorizationService == null) {
            authorizationService = getOptionalBean(builder, LightningAuthenticationTokenService.class);
            if (authorizationService == null) {
                authorizationService = new DefaultAuthenticationTokenService();
            }

            builder.setSharedObject(LightningAuthenticationTokenService.class, authorizationService);
        }

        return authorizationService;
    }


    static <B extends HttpSecurityBuilder<B>> LightningTokenGenerator<? extends LightningToken> getTokenGenerator(B builder) {
        LightningTokenGenerator<? extends LightningToken> tokenGenerator = (LightningTokenGenerator<? extends LightningToken>) builder.getSharedObject(LightningTokenGenerator.class);
        TokenSettingsProvider settingsProvider = builder.getSharedObject(TokenSettingsProvider.class);
        if (tokenGenerator == null) {
            tokenGenerator = (LightningTokenGenerator<? extends LightningToken>) getOptionalBean(builder, LightningTokenGenerator.class);
            if (tokenGenerator == null) {
                LightningJwtGenerator jwtGenerator = getJwtGenerator(builder);
                DefaultLightningAccessTokenGenerator accessTokenGenerator = new DefaultLightningAccessTokenGenerator();
                LightningTokenCustomizer<LightningTokenClaimsContext> accessTokenCustomizer = getAccessTokenCustomizer(builder);
                if (accessTokenCustomizer != null) {
                    accessTokenGenerator.setAccessTokenCustomizer(new DelegateLightningTokenCustomizer<>(
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
                                    new DefaultTokenDetailAwareTokenCustomizer(builder.getSharedObject(TokenSettingsProvider.class))::customize,
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
            sharedObject = getOptionalBean(builder, type);
            if (sharedObject != null) {
                builder.setSharedObject(TokenSettingsProvider.class, sharedObject);
            }
        }
        return sharedObject;
    }


    static <B extends HttpSecurityBuilder<B>, T> T getBean(B builder, Class<T> type) {
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
}
