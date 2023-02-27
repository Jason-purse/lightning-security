package com.generatera.oauth2.resource.server.config;

import com.generatera.oauth2.resource.server.config.token.JwtDecoderDirectConfiguration;
import com.generatera.oauth2.resource.server.config.token.JwtTokenVerificationConfiguration;
import com.generatera.oauth2.resource.server.config.token.OpaqueTokenVerificationConfiguration;
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
                    JwtDecoderDirectConfiguration.class.getName()
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
    }
}
