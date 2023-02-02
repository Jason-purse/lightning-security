package com.generatera.central.oauth2.authorization.server.configuration.components.token;

import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;

import java.util.Arrays;
import java.util.List;

public class DelegateCentralOauth2TokenCustomizer<T extends OAuth2TokenContext> implements LightningCentralOAuth2TokenCustomizer<T>{

    private final List<LightningCentralOAuth2TokenCustomizer<T>> customizers;

    @SafeVarargs
    public DelegateCentralOauth2TokenCustomizer(LightningCentralOAuth2TokenCustomizer<T> ... customizers) {
        this.customizers = Arrays.asList(customizers);
    }
    @Override
    public void customize(T context) {
        for (LightningCentralOAuth2TokenCustomizer<T> customizer : customizers) {
            customizer.customize(context);
        }
    }
}
