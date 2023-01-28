package com.generatera.central.oauth2.authorization.server.configuration;

import org.springframework.security.oauth2.server.authorization.config.ConfigurationSettingNames;

/**
 * @author FLJ
 * @date 2023/1/18
 * @time 14:02
 * @Description ConfigurationSettings
 */
public class OAuth2ConfigurationSettingNames {

    public static class Token {
        public final static String ID_TOKEN_SIGNATURE_ALGORITHM = ConfigurationSettingNames.Token.ID_TOKEN_SIGNATURE_ALGORITHM;
    }

    public static final class Provider {
        public static final String AUTHORIZATION_ENDPOINT;
        public static final String TOKEN_ENDPOINT;
        public static final String OIDC_CLIENT_REGISTRATION_ENDPOINT;
        public static final String OIDC_USER_INFO_ENDPOINT;

        private Provider() {
        }

        static {
            AUTHORIZATION_ENDPOINT = ConfigurationSettingNames.Provider.AUTHORIZATION_ENDPOINT;
            TOKEN_ENDPOINT = ConfigurationSettingNames.Provider.TOKEN_ENDPOINT;
            OIDC_CLIENT_REGISTRATION_ENDPOINT = ConfigurationSettingNames.Provider.OIDC_CLIENT_REGISTRATION_ENDPOINT;
            OIDC_USER_INFO_ENDPOINT = ConfigurationSettingNames.Provider.OIDC_USER_INFO_ENDPOINT;
        }
    }
}
