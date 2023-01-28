package com.generatera.security.authorization.server.specification.components.provider;

import com.generatera.security.authorization.server.specification.AbstractSettings;
import com.generatera.security.authorization.server.specification.ConfigurationSettingNames;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * 剥离 oauth2 providerSettings ...
 *
 * oauth2 ProviderSettings copy ...
 */
public  class ProviderSettings extends AbstractSettings {

    protected ProviderSettings(Map<String, Object> settings) {
        super(settings);
    }

    public String getIssuer() {
        return this.getSetting(ConfigurationSettingNames.Provider.ISSUER);
    }


    public String getJwkSetEndpoint() {
        return this.getSetting(ConfigurationSettingNames.Provider.JWK_SET_ENDPOINT);
    }

    public String getTokenEndpoint() {
        return this.getSetting(ConfigurationSettingNames.Provider.TOKEN_ENDPOINT);
    }

    public String getTokenRevocationEndpoint() {
        return this.getSetting(ConfigurationSettingNames.Provider.TOKEN_REVOCATION_ENDPOINT);
    }

    public String getTokenIntrospectionEndpoint() {
        return this.getSetting(ConfigurationSettingNames.Provider.TOKEN_INTROSPECTION_ENDPOINT);
    }


    public static Builder builder() {
        return (new Builder())
                .jwkSetEndpoint(ProviderSettingProperties.JWT_SET_ENDPOINT)
                .tokenRevocationEndpoint(ProviderSettingProperties.TOKEN_REVOCATION_ENDPOINT)
                .tokenIntrospectionEndpoint(ProviderSettingProperties.TOKEN_INTROSPECTION_ENDPOINT);
    }

    public static Builder withSettings(Map<String, Object> settings) {
        Assert.notEmpty(settings, "settings cannot be empty");
        return (new Builder()).settings((s) -> {
            s.putAll(settings);
        });
    }

    public static class Builder extends AbstractBuilder<ProviderSettings, Builder> {
        protected Builder() {
        }

        public Builder issuer(String issuer) {
            return this.setting(ConfigurationSettingNames.Provider.ISSUER, issuer);
        }


        public Builder jwkSetEndpoint(String jwkSetEndpoint) {
            return this.setting(ConfigurationSettingNames.Provider.JWK_SET_ENDPOINT, jwkSetEndpoint);
        }

        public Builder tokenRevocationEndpoint(String tokenRevocationEndpoint) {
            return this.setting(ConfigurationSettingNames.Provider.TOKEN_REVOCATION_ENDPOINT, tokenRevocationEndpoint);
        }

        public Builder tokenIntrospectionEndpoint(String tokenIntrospectionEndpoint) {
            return this.setting(ConfigurationSettingNames.Provider.TOKEN_INTROSPECTION_ENDPOINT, tokenIntrospectionEndpoint);
        }

        public Builder tokenEndpoint(String tokenEndpoint) {
            return this.setting(ConfigurationSettingNames.Provider.TOKEN_ENDPOINT,tokenEndpoint);
        }

        public ProviderSettings build() {
            return new ProviderSettings(this.getSettings());
        }
    }
}