package com.generatera.authorization.server.common.configuration.ext.oauth2.provider;

import org.springframework.util.Assert;
/**
 * @author FLJ
 * @date 2023/1/4
 * @time 10:08
 * @Description 仅仅只是为了从bean name层面上 避免和 oauth provider settings  产生歧义 ..
 */
public class ProviderSettingsProvider {

    private final ProviderSettings providerSettings;

    public ProviderSettingsProvider(ProviderSettings providerSettings) {
        Assert.notNull(providerSettings,"providerSettings must not be null !!!");
        this.providerSettings = providerSettings;
    }

    public ProviderSettings getProviderSettings() {
        return providerSettings;
    }
}
