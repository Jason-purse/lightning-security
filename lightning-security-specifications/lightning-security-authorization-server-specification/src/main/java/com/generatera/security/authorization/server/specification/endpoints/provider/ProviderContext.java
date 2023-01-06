package com.generatera.security.authorization.server.specification.endpoints.provider;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.function.Supplier;

public final class ProviderContext {
    private final ProviderSettings providerSettings;
    private final Supplier<String> issuerSupplier;

    public ProviderContext(ProviderSettings providerSettings, @Nullable Supplier<String> issuerSupplier) {
        Assert.notNull(providerSettings, "providerSettings cannot be null");
        this.providerSettings = providerSettings;
        this.issuerSupplier = issuerSupplier;
    }

    public ProviderSettings getProviderSettings() {
        return this.providerSettings;
    }

    public String getIssuer() {
        return this.issuerSupplier != null ? (String)this.issuerSupplier.get() : this.getProviderSettings().getIssuer();
    }
}