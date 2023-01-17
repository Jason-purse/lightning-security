package com.generatera.security.authorization.server.specification.components.token;

import java.util.Arrays;
import java.util.List;

public class DelegateLightningTokenCustomizer<T extends LightningTokenContext> implements LightningTokenCustomizer<T> {

    private final List<LightningTokenCustomizer<T>> tokenCustomizers;

    @SafeVarargs
    public DelegateLightningTokenCustomizer(LightningTokenCustomizer<T> ... tokenCustomizers) {
        this.tokenCustomizers = Arrays.asList(tokenCustomizers);
    }
    @Override
    public void customize(T tokenContext) {
        for (LightningTokenCustomizer<T> tokenCustomizer : tokenCustomizers) {
            tokenCustomizer.customize(tokenContext);
        }
    }
}
