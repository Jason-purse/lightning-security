package com.generatera.authorization.application.server.config;

import com.generatera.security.authorization.server.specification.components.token.format.jwt.JWKSourceProvider;
import com.nimbusds.jose.jwk.source.JWKSource;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public class ApplicationAuthExtConfigurerUtils {

    public static JWKSourceProvider getJwkSourceProvider(HttpSecurity security) {
        JWKSourceProvider provider = security.getSharedObject(JWKSourceProvider.class);
        if(provider == null) {
            ApplicationContext applicationContext = getApplicationContext(security);
            JWKSourceProvider sourceProvider = applicationContext.getBean(JWKSourceProvider.class);
            security.setSharedObject(JWKSourceProvider.class,sourceProvider);
            // override
            security.setSharedObject(JWKSource.class,sourceProvider.getJWKSource());
            provider = sourceProvider;
        }
        return provider;
    }

    public static ApplicationContext getApplicationContext(HttpSecurity security) {
        return security.getSharedObject(ApplicationContext.class);
    }

}
