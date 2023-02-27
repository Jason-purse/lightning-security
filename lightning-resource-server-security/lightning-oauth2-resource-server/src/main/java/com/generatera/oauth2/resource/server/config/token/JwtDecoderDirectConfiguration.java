package com.generatera.oauth2.resource.server.config.token;

import com.generatera.oauth2.resource.server.config.token.jose.NimbusJwtDecoderExtUtils;
import com.generatera.resource.server.config.ResourceServerProperties;
import com.generatera.resource.server.config.ResourceServerProperties.TokenVerificationConfig;
import com.generatera.security.authorization.server.specification.HandlerFactory;
import com.generatera.security.authorization.server.specification.HandlerFactory.HandlerProvider;
import com.jianyue.lightning.boot.starter.util.dataflow.impl.Tuple;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.Assert;

import java.util.List;

@AutoConfiguration
public class JwtDecoderDirectConfiguration {

    @Bean
    public JwtDecoder jwtDecoder(ResourceServerProperties resourceServerProperties) {
        return HandlerFactory
                .getRequiredHandler(
                        JwtDecoder.class,
                        resourceServerProperties
                )
                .getHandler()
                .<HandlerFactory.TransformHandler<ResourceServerProperties, JwtDecoder>>nativeHandler()
                .get(resourceServerProperties);
    }

    static {
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


