package com.generatera.resource.server.config.method.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@Configuration
public class MethodSecurityMetadataSourceConfiguration {

    @AutoConfiguration
    @EntityScan(basePackages = "com.generatera.resource.server.config.method.security.entity")
    @EnableJpaRepositories(basePackages = "com.generatera.resource.server.config.method.security.repository")
    public static class JapMethodSecurityMetadataSourceConfiguration {

    }
}
