package com.generatera.oauth2.resource.server.config.token;

import com.generatera.resource.server.config.ResourceServerProperties;
import com.generatera.security.authorization.server.specification.HandlerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;


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
}