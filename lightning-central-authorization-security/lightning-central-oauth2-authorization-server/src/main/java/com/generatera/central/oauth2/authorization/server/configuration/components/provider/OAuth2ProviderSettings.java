package com.generatera.central.oauth2.authorization.server.configuration.components.provider;

import com.generatera.security.authorization.server.specification.ConfigurationSettingNames;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettings;

import java.util.Map;
/**
 * @author FLJ
 * @date 2023/1/17
 * @time 14:25
 * @Description OAuth2ProviderSettings
 *
 * 扩展了 ProviderSettings ...
 *
 * // TODO: 2023/1/17  需要不断和{@link org.springframework.security.oauth2.server.authorization.config.ProviderSettings} 做好同步
 */
public class OAuth2ProviderSettings extends ProviderSettings {

    protected OAuth2ProviderSettings(Map<String, Object> settings) {
        super(settings);
    }

    public String getAuthorizationEndpoint() {
        return this.getSetting(ConfigurationSettingNames.Provider.AUTHORIZATION_ENDPOINT);
    }

    public String getTokenEndpoint() {
        return this.getSetting(ConfigurationSettingNames.Provider.TOKEN_ENDPOINT);
    }

    public String getOidcClientRegistrationEndpoint() {
        return this.getSetting(ConfigurationSettingNames.Provider.OIDC_CLIENT_REGISTRATION_ENDPOINT);
    }

    public String getOidcUserInfoEndpoint() {
        return this.getSetting(ConfigurationSettingNames.Provider.OIDC_USER_INFO_ENDPOINT);
    }


    public static class Builder extends ProviderSettings.Builder {

        private Builder() {
        }

        @Override
        public Builder issuer(String issuer) {
            return (Builder) super.issuer(issuer);
        }

        @Override
        public Builder jwkSetEndpoint(String jwkSetEndpoint) {
            return (Builder)super.jwkSetEndpoint(jwkSetEndpoint);
        }

        @Override
        public Builder tokenRevocationEndpoint(String tokenRevocationEndpoint) {
            return (Builder)super.tokenRevocationEndpoint(tokenRevocationEndpoint);
        }

        @Override
        public Builder tokenIntrospectionEndpoint(String tokenIntrospectionEndpoint) {
            return (Builder)super.tokenIntrospectionEndpoint(tokenIntrospectionEndpoint);
        }

        public Builder authorizationEndpoint(String authorizationEndpoint) {
            return (Builder)this.setting(ConfigurationSettingNames.Provider.AUTHORIZATION_ENDPOINT, authorizationEndpoint);
        }

        public Builder tokenEndpoint(String tokenEndpoint) {
            return (Builder)this.setting(ConfigurationSettingNames.Provider.TOKEN_ENDPOINT, tokenEndpoint);
        }

        public Builder oidcClientRegistrationEndpoint(String oidcClientRegistrationEndpoint) {
            return (Builder)this.setting(ConfigurationSettingNames.Provider.OIDC_CLIENT_REGISTRATION_ENDPOINT, oidcClientRegistrationEndpoint);
        }

        public Builder oidcUserInfoEndpoint(String oidcUserInfoEndpoint) {
            return (Builder)this.setting(ConfigurationSettingNames.Provider.OIDC_USER_INFO_ENDPOINT, oidcUserInfoEndpoint);
        }

        public OAuth2ProviderSettings build() {
            return new OAuth2ProviderSettings(this.getSettings());
        }
    }

}
