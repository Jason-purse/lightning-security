package com.generatera.security.authorization.server.specification.endpoints.provider;

import com.generatera.security.authorization.server.specification.AbstractSettings;
import com.generatera.security.authorization.server.specification.ConfigurationSettingNames;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * 剥离 oauth2 providerSettings ...
 *
 * oauth2 ProviderSettings copy ...
 */
public final class ProviderSettings extends AbstractSettings {
    private ProviderSettings(Map<String, Object> settings) {
        super(settings);
    }

    public String getIssuer() {
        return (String)this.getSetting(ConfigurationSettingNames.Provider.ISSUER);
    }

    public String getAuthorizationEndpoint() {
        return (String)this.getSetting(ConfigurationSettingNames.Provider.AUTHORIZATION_ENDPOINT);
    }

    public String getTokenEndpoint() {
        return (String)this.getSetting(ConfigurationSettingNames.Provider.TOKEN_ENDPOINT);
    }

    public String getJwkSetEndpoint() {
        return (String)this.getSetting(ConfigurationSettingNames.Provider.JWK_SET_ENDPOINT);
    }

    public String getTokenRevocationEndpoint() {
        return (String)this.getSetting(ConfigurationSettingNames.Provider.TOKEN_REVOCATION_ENDPOINT);
    }

    public String getTokenIntrospectionEndpoint() {
        return (String)this.getSetting(ConfigurationSettingNames.Provider.TOKEN_INTROSPECTION_ENDPOINT);
    }

    public String getOidcClientRegistrationEndpoint() {
        return (String)this.getSetting(ConfigurationSettingNames.Provider.OIDC_CLIENT_REGISTRATION_ENDPOINT);
    }

    public String getOidcUserInfoEndpoint() {
        return (String)this.getSetting(ConfigurationSettingNames.Provider.OIDC_USER_INFO_ENDPOINT);
    }

    public static Builder builder() {
        return (new Builder()).authorizationEndpoint("/oauth2/authorize").tokenEndpoint("/oauth2/token").jwkSetEndpoint("/oauth2/jwks").tokenRevocationEndpoint("/oauth2/revoke").tokenIntrospectionEndpoint("/oauth2/introspect").oidcClientRegistrationEndpoint("/connect/register").oidcUserInfoEndpoint("/userinfo");
    }

    public static Builder withSettings(Map<String, Object> settings) {
        Assert.notEmpty(settings, "settings cannot be empty");
        return (Builder)(new Builder()).settings((s) -> {
            s.putAll(settings);
        });
    }

    public static class Builder extends AbstractBuilder<ProviderSettings, Builder> {
        private Builder() {
        }

        public Builder issuer(String issuer) {
            return (Builder)this.setting(ConfigurationSettingNames.Provider.ISSUER, issuer);
        }

        public Builder authorizationEndpoint(String authorizationEndpoint) {
            return (Builder)this.setting(ConfigurationSettingNames.Provider.AUTHORIZATION_ENDPOINT, authorizationEndpoint);
        }

        public Builder tokenEndpoint(String tokenEndpoint) {
            return (Builder)this.setting(ConfigurationSettingNames.Provider.TOKEN_ENDPOINT, tokenEndpoint);
        }

        public Builder jwkSetEndpoint(String jwkSetEndpoint) {
            return (Builder)this.setting(ConfigurationSettingNames.Provider.JWK_SET_ENDPOINT, jwkSetEndpoint);
        }

        public Builder tokenRevocationEndpoint(String tokenRevocationEndpoint) {
            return (Builder)this.setting(ConfigurationSettingNames.Provider.TOKEN_REVOCATION_ENDPOINT, tokenRevocationEndpoint);
        }

        public Builder tokenIntrospectionEndpoint(String tokenIntrospectionEndpoint) {
            return (Builder)this.setting(ConfigurationSettingNames.Provider.TOKEN_INTROSPECTION_ENDPOINT, tokenIntrospectionEndpoint);
        }

        public Builder oidcClientRegistrationEndpoint(String oidcClientRegistrationEndpoint) {
            return (Builder)this.setting(ConfigurationSettingNames.Provider.OIDC_CLIENT_REGISTRATION_ENDPOINT, oidcClientRegistrationEndpoint);
        }

        public Builder oidcUserInfoEndpoint(String oidcUserInfoEndpoint) {
            return (Builder)this.setting(ConfigurationSettingNames.Provider.OIDC_USER_INFO_ENDPOINT, oidcUserInfoEndpoint);
        }

        public ProviderSettings build() {
            return new ProviderSettings(this.getSettings());
        }
    }
}