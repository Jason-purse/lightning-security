package com.generatera.oauth2.resource.server.config;

import com.generatera.oauth2.resource.server.config.token.JwtTokenVerificationConfiguration;
import com.generatera.oauth2.resource.server.config.token.OpaqueTokenVerificationConfiguration;
import com.generatera.oauth2.resource.server.config.token.jose.NimbusJwtDecoderExtUtils;
import com.generatera.resource.server.config.ResourceServerProperties;
import com.generatera.resource.server.config.ResourceServerProperties.TokenVerificationConfig;
import com.generatera.security.authorization.server.specification.HandlerFactory;
import com.generatera.security.authorization.server.specification.HandlerFactory.HandlerProvider;
import com.generatera.security.authorization.server.specification.HandlerFactory.SupplierHandler;
import com.generatera.security.authorization.server.specification.PropertiesBindImportSelector;
import com.jianyue.lightning.boot.starter.util.dataflow.impl.Tuple;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.Assert;

import java.util.LinkedList;
import java.util.List;

public class OAuth2ResourceServerComponentsImportSelector extends PropertiesBindImportSelector<ResourceServerProperties> {

    public OAuth2ResourceServerComponentsImportSelector(BeanFactory beanFactory, Environment environment) {
        super(beanFactory, environment);
    }

    @NotNull
    @Override
    public String[] selectImports(@NotNull AnnotationMetadata importingClassMetadata) {
        ResourceServerProperties properties = getProperties();
        List<String> candidates = new LinkedList<>();
        tokenVerificationConfig(properties, candidates);
        return candidates.size() > 0 ? candidates.toArray(String[]::new) : new String[0];
    }

    private static void tokenVerificationConfig(ResourceServerProperties properties, List<String> candidates) {
        candidates.add(
                HandlerFactory
                        .getRequiredHandler(TokenVerificationConfig.class,
                                properties.getTokenVerificationConfig().getTokenType()
                        )
                        .getHandler()
                        .<SupplierHandler<String>>nativeHandler()
                        .get()
        );

        // 直接配置 jwtdecoder 处理 ..
        // 如果合适 ...
        if (properties.getTokenVerificationConfig().getJwkSourceCategory() != TokenVerificationConfig.JwkSourceCategory.JWK_OR_ISSUER_URL) {
            candidates.add(
                    JwtTokenVerificationConfiguration.JwtDecoderDirectConfiguration.class.getName()
            );
        }
    }

    static {
        HandlerFactory.registerHandlers(
                HandlerProvider.list(
                        TokenVerificationConfig.class,
                        List.of(
                                // opaque
                                new Tuple<>(
                                        TokenVerificationConfig.TokenType.Opaque::equals,
                                        SupplierHandler.of(
                                                OpaqueTokenVerificationConfiguration.class::getName
                                        )
                                ),
                                // jwt
                                new Tuple<>(
                                        TokenVerificationConfig.TokenType.JWT::equals,
                                        SupplierHandler.of(
                                                JwtTokenVerificationConfiguration.class::getName
                                        )
                                ),
                                // fallback
                                new Tuple<>(
                                        value -> true,
                                        SupplierHandler.of(JwtTokenVerificationConfiguration.class::getName)
                                )
                        )
                ));

        // JWT decoder 进一步配置
        HandlerFactory.registerHandlers(
                HandlerProvider.list(
                        JwtDecoder.class,
                        List.of(
                                // rsa 256
                                new Tuple<>(
                                        TokenVerificationConfig.JwkSourceCategory.RSA256::equals,
                                        HandlerFactory.TransformHandler.<ResourceServerProperties, NimbusJwtDecoder>of(
                                                (resourceServerProperties) -> {
                                                    TokenVerificationConfig.Rsa256Jwk rsa256Jwk = resourceServerProperties.getTokenVerificationConfig().getRsa256Jwk();
                                                    Assert.notNull(rsa256Jwk.getValue(), "public rsa key must not be null !!!");
                                                    return NimbusJwtDecoderExtUtils.fromPublicRsaKey(rsa256Jwk.getValue());
                                                }
                                        )
                                ),
                                // secret
                                new Tuple<>(
                                        TokenVerificationConfig.JwkSourceCategory.SECRET::equals,
                                        HandlerFactory.TransformHandler.<ResourceServerProperties, NimbusJwtDecoder>of(
                                                (resourceServerProperties) -> {
                                                    TokenVerificationConfig.SecretJwk secretJwk = resourceServerProperties.getTokenVerificationConfig().getSecretJwk();
                                                    Assert.notNull(secretJwk.getValue(), "secret key must not be null !!");
                                                    Assert.notNull(secretJwk.getAlgorithm(), "secret algorithm must not be null !!");
                                                    return NimbusJwtDecoderExtUtils.fromSecretKey(secretJwk.getValue(), secretJwk.getAlgorithm());
                                                }
                                        )
                                )
                        )
                )
        );
    }
}
