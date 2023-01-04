package com.generatera.authorization.server.oauth2.configuration;

import com.generatera.authorization.application.server.config.token.LightningOAuth2ServerTokenGenerator;
import com.generatera.authorization.server.oauth2.configuration.token.DefaultLightningOAuth2ServerTokenGenerator;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
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
 */
final class OAuth2ConfigurerExtUtils {
        private OAuth2ConfigurerExtUtils() {
        }

        static <B extends HttpSecurityBuilder<B>> OAuth2TokenGenerator<? extends OAuth2Token> getTokenGenerator(B builder) {
            OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator = (OAuth2TokenGenerator<? extends OAuth2Token>)builder.getSharedObject(OAuth2TokenGenerator.class);
            if (tokenGenerator == null) {
                tokenGenerator = (OAuth2TokenGenerator<? extends OAuth2Token>)getOptionalBean(builder, OAuth2TokenGenerator.class);
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

                // 处理掉 ..
                builder.setSharedObject(OAuth2TokenGenerator.class, tokenGenerator);
                builder.setSharedObject(LightningOAuth2ServerTokenGenerator.class,new DefaultLightningOAuth2ServerTokenGenerator(tokenGenerator));
            }

            return tokenGenerator;
        }

        private static <B extends HttpSecurityBuilder<B>> JwtGenerator getJwtGenerator(B builder) {
            JwtGenerator jwtGenerator = (JwtGenerator)builder.getSharedObject(JwtGenerator.class);
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
            JwtEncoder jwtEncoder = (JwtEncoder)builder.getSharedObject(JwtEncoder.class);
            if (jwtEncoder == null) {
                jwtEncoder = (JwtEncoder)getOptionalBean(builder, JwtEncoder.class);
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

            return (JwtEncoder)jwtEncoder;
        }

        static <B extends HttpSecurityBuilder<B>> JWKSource<SecurityContext> getJwkSource(B builder) {
            JWKSource<SecurityContext> jwkSource = (JWKSource)builder.getSharedObject(JWKSource.class);
            if (jwkSource == null) {
                ResolvableType type = ResolvableType.forClassWithGenerics(JWKSource.class, new Class[]{SecurityContext.class});
                jwkSource = (JWKSource)getOptionalBean(builder, type);
                if (jwkSource != null) {
                    builder.setSharedObject(JWKSource.class, jwkSource);
                }
            }

            return jwkSource;
        }

        private static <B extends HttpSecurityBuilder<B>> OAuth2TokenCustomizer<JwtEncodingContext> getJwtCustomizer(B builder) {
            ResolvableType type = ResolvableType.forClassWithGenerics(OAuth2TokenCustomizer.class, new Class[]{JwtEncodingContext.class});
            return (OAuth2TokenCustomizer)getOptionalBean(builder, type);
        }

        private static <B extends HttpSecurityBuilder<B>> OAuth2TokenCustomizer<OAuth2TokenClaimsContext> getAccessTokenCustomizer(B builder) {
            ResolvableType type = ResolvableType.forClassWithGenerics(OAuth2TokenCustomizer.class, new Class[]{OAuth2TokenClaimsContext.class});
            return (OAuth2TokenCustomizer)getOptionalBean(builder, type);
        }


        static <B extends HttpSecurityBuilder<B>, T> T getBean(B builder, Class<T> type) {
            return ((ApplicationContext)builder.getSharedObject(ApplicationContext.class)).getBean(type);
        }

        static <B extends HttpSecurityBuilder<B>, T> T getBean(B builder, ResolvableType type) {
            ApplicationContext context = (ApplicationContext)builder.getSharedObject(ApplicationContext.class);
            String[] names = context.getBeanNamesForType(type);
            if (names.length == 1) {
                return (T)context.getBean(names[0]);
            } else if (names.length > 1) {
                throw new NoUniqueBeanDefinitionException(type, names);
            } else {
                throw new NoSuchBeanDefinitionException(type);
            }
        }

        static <B extends HttpSecurityBuilder<B>, T> T getOptionalBean(B builder, Class<T> type) {
            Map<String, T> beansMap = BeanFactoryUtils.beansOfTypeIncludingAncestors((ListableBeanFactory)builder.getSharedObject(ApplicationContext.class), type);
            if (beansMap.size() > 1) {
                int var10003 = beansMap.size();
                String var10004 = type.getName();
                throw new NoUniqueBeanDefinitionException(type, var10003, "Expected single matching bean of type '" + var10004 + "' but found " + beansMap.size() + ": " + StringUtils.collectionToCommaDelimitedString(beansMap.keySet()));
            } else {
                return !beansMap.isEmpty() ? beansMap.values().iterator().next() : null;
            }
        }

        static <B extends HttpSecurityBuilder<B>, T> T getOptionalBean(B builder, ResolvableType type) {
            ApplicationContext context = (ApplicationContext)builder.getSharedObject(ApplicationContext.class);
            String[] names = context.getBeanNamesForType(type);
            if (names.length > 1) {
                throw new NoUniqueBeanDefinitionException(type, names);
            } else {
                return names.length == 1 ? (T)context.getBean(names[0]) : null;
            }
        }
}
