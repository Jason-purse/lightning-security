package com.generatera.central.oauth2.authorization.server.configuration;

import com.generatera.authorization.server.common.configuration.LightningCentralAuthServer;
import com.generatera.authorization.server.common.configuration.OAuth2AuthorizationServer;
import com.generatera.central.oauth2.authorization.server.configuration.components.token.LightningCentralOAuth2TokenCustomizer;
import com.generatera.security.authorization.server.specification.util.HttpSecurityBuilderUtils;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author FLJ
 * @date 2023/1/4
 * @time 10:32
 * @Description 主要是处理 token 生成器 ..
 * <p>
 * 给出我们自己自定义 OAuth2 TokenGeneartor的机会 ..
 * 不再使用 spring-security-oauth2 自己的 token 生成器的默认逻辑 。。。
 */
final class OAuth2AuthorizationServerConfigurerExtUtils {
    private OAuth2AuthorizationServerConfigurerExtUtils() {
    }

    @SuppressWarnings("unchecked")
    static <B extends HttpSecurityBuilder<B>> OAuth2AuthorizationServerConfigurer<B> getOAuth2AuthorizationServerConfigurer(B builder) {
        OAuth2AuthorizationServerConfigurer<B> oAuth2AuthorizationServerConfigurer = builder.getSharedObject(OAuth2AuthorizationServerConfigurer.class);
        if (oAuth2AuthorizationServerConfigurer == null) {
            OAuth2AuthorizationServerConfigurer<B> serverConfigurer = new OAuth2AuthorizationServerConfigurer<>();
            builder.setSharedObject(OAuth2AuthorizationServerConfigurer.class, serverConfigurer);
            // 用于判断oauth2 authorization server是否存在
            builder.setSharedObject(LightningCentralAuthServer.class, new OAuth2AuthorizationServer() {
            });
            oAuth2AuthorizationServerConfigurer = serverConfigurer;
        }
        return oAuth2AuthorizationServerConfigurer;
    }

    /**
     * 获取 token 生成器 ..
     *
     * 此token 生成器将 委派所有token 生成任务到具体的token 生成器实现 ..
     * 然后,最终的出token数据 ...
     */
    static <B extends HttpSecurityBuilder<B>> OAuth2TokenGenerator<? extends OAuth2Token> getTokenGenerator(B builder) {
        // 是否存在共享对象 ...
        OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator = (OAuth2TokenGenerator<? extends OAuth2Token>) builder.getSharedObject(OAuth2TokenGenerator.class);
        if (tokenGenerator == null) {
            tokenGenerator = (OAuth2TokenGenerator<? extends OAuth2Token>) getOptionalBean(builder, OAuth2TokenGenerator.class);
            if (tokenGenerator == null) {
                JwtGenerator jwtGenerator = getJwtGenerator(builder);
                OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
                OAuth2TokenCustomizer<OAuth2TokenClaimsContext> accessTokenCustomizer = getAccessTokenCustomizer(builder);
                if (accessTokenCustomizer != null) {
                    accessTokenGenerator.setAccessTokenCustomizer(accessTokenCustomizer);
                }

                OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
                if (jwtGenerator != null) {
                    tokenGenerator = new DelegatingOAuth2TokenGenerator(jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
                } else {
                    tokenGenerator = new DelegatingOAuth2TokenGenerator(accessTokenGenerator, refreshTokenGenerator);
                }
            }
            builder.setSharedObject(OAuth2TokenGenerator.class, tokenGenerator);
        }

        return tokenGenerator;
    }

    private static <B extends HttpSecurityBuilder<B>> JwtGenerator getJwtGenerator(B builder) {
        // 共享对象中,是否存在 jwt 生成器 ...
        JwtGenerator jwtGenerator = builder.getSharedObject(JwtGenerator.class);
        if (jwtGenerator == null) {
            JwtEncoder jwtEncoder = getJwtEncoder(builder);
            if (jwtEncoder != null) {
                jwtGenerator = new JwtGenerator(jwtEncoder);
                OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer = getJwtCustomizer(builder);
                if (jwtCustomizer != null) {
                    jwtGenerator.setJwtCustomizer(jwtCustomizer);
                }

                builder.setSharedObject(JwtGenerator.class, jwtGenerator);
            }
        }

        return jwtGenerator;
    }

    private static <B extends HttpSecurityBuilder<B>> JwtEncoder getJwtEncoder(B builder) {
        JwtEncoder jwtEncoder = builder.getSharedObject(JwtEncoder.class);
        if (jwtEncoder == null) {
            jwtEncoder = getOptionalBean(builder, JwtEncoder.class);
            if (jwtEncoder == null) {
                JWKSource<SecurityContext> jwkSource = getJwkSource(builder);
                if (jwkSource != null) {
                    jwtEncoder = new NimbusJwtEncoder(jwkSource);
                }
            }

            if (jwtEncoder != null) {
                builder.setSharedObject(JwtEncoder.class, jwtEncoder);
            }
        }

        return jwtEncoder;
    }

    static <B extends HttpSecurityBuilder<B>> JWKSource<SecurityContext> getJwkSource(B builder) {
        JWKSource<SecurityContext> jwkSource = (JWKSource) builder.getSharedObject(JWKSource.class);
        if (jwkSource == null) {
            ResolvableType type = ResolvableType.forClassWithGenerics(JWKSource.class, SecurityContext.class);
            jwkSource = getOptionalBean(builder, type);
            if (jwkSource != null) {
                builder.setSharedObject(JWKSource.class, jwkSource);
            }
        }

        return jwkSource;
    }

    private static <B extends HttpSecurityBuilder<B>> OAuth2TokenCustomizer<JwtEncodingContext> getJwtCustomizer(B builder) {
        try {
            return HttpSecurityBuilderUtils.getSharedOrCtxBean(builder, LightningCentralOAuth2TokenCustomizer.LightningCentralOAuth2JwtTokenCustomizer.class);
        } catch (Exception e) {
            ResolvableType type = ResolvableType.forClassWithGenerics(OAuth2TokenCustomizer.class, JwtEncodingContext.class);
            return getOptionalBean(builder, type);
        }
    }

    private static <B extends HttpSecurityBuilder<B>> OAuth2TokenCustomizer<OAuth2TokenClaimsContext> getAccessTokenCustomizer(B builder) {
        try {
            return HttpSecurityBuilderUtils.getSharedOrCtxBean(builder, LightningCentralOAuth2TokenCustomizer.LightningCentralOAuth2AccessTokenCustomizer.class);

        } catch (Exception e) {
            ResolvableType type = ResolvableType.forClassWithGenerics(OAuth2TokenCustomizer.class, OAuth2TokenClaimsContext.class);
            return getBean(builder, type);
        }
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
