package com.generatera.security.authorization.server.specification;

public final class ConfigurationSettingNames {
    private static final String SETTINGS_NAMESPACE = "settings.";

    private ConfigurationSettingNames() {
    }

    public static final class Token {
        private static final String TOKEN_SETTINGS_NAMESPACE = "settings.".concat("token.");
        public static final String ACCESS_TOKEN_TIME_TO_LIVE;
        public static final String ACCESS_TOKEN_FORMAT;
        public static final String REUSE_REFRESH_TOKENS;
        public static final String REFRESH_TOKEN_TIME_TO_LIVE;
        public static final String ID_TOKEN_SIGNATURE_ALGORITHM;
        public static final String AUDIENCE;

        private Token() {
        }

        static {
            ACCESS_TOKEN_TIME_TO_LIVE = TOKEN_SETTINGS_NAMESPACE.concat("access-token-time-to-live");
            ACCESS_TOKEN_FORMAT = TOKEN_SETTINGS_NAMESPACE.concat("access-token-format");
            REUSE_REFRESH_TOKENS = TOKEN_SETTINGS_NAMESPACE.concat("reuse-refresh-tokens");
            REFRESH_TOKEN_TIME_TO_LIVE = TOKEN_SETTINGS_NAMESPACE.concat("refresh-token-time-to-live");
            ID_TOKEN_SIGNATURE_ALGORITHM = TOKEN_SETTINGS_NAMESPACE.concat("id-token-signature-algorithm");
            AUDIENCE = TOKEN_SETTINGS_NAMESPACE.concat("token-audience");
        }
    }

    public static final class Provider {
        private static final String PROVIDER_SETTINGS_NAMESPACE = "settings.".concat("provider.");
        public static final String ISSUER;
        public static final String AUTHORIZATION_ENDPOINT;
        public static final String TOKEN_ENDPOINT;
        public static final String JWK_SET_ENDPOINT;
        public static final String TOKEN_REVOCATION_ENDPOINT;
        public static final String TOKEN_INTROSPECTION_ENDPOINT;
        public static final String OIDC_CLIENT_REGISTRATION_ENDPOINT;
        public static final String OIDC_USER_INFO_ENDPOINT;

        private Provider() {
        }

        static {
            ISSUER = PROVIDER_SETTINGS_NAMESPACE.concat("issuer");
            AUTHORIZATION_ENDPOINT = PROVIDER_SETTINGS_NAMESPACE.concat("authorization-endpoint");
            TOKEN_ENDPOINT = PROVIDER_SETTINGS_NAMESPACE.concat("token-endpoint");
            JWK_SET_ENDPOINT = PROVIDER_SETTINGS_NAMESPACE.concat("jwk-set-endpoint");
            TOKEN_REVOCATION_ENDPOINT = PROVIDER_SETTINGS_NAMESPACE.concat("token-revocation-endpoint");
            TOKEN_INTROSPECTION_ENDPOINT = PROVIDER_SETTINGS_NAMESPACE.concat("token-introspection-endpoint");
            OIDC_CLIENT_REGISTRATION_ENDPOINT = PROVIDER_SETTINGS_NAMESPACE.concat("oidc-client-registration-endpoint");
            OIDC_USER_INFO_ENDPOINT = PROVIDER_SETTINGS_NAMESPACE.concat("oidc-user-info-endpoint");
        }
    }

    public static final class Client {
        private static final String CLIENT_SETTINGS_NAMESPACE = "settings.".concat("client.");
        public static final String REQUIRE_PROOF_KEY;
        public static final String REQUIRE_AUTHORIZATION_CONSENT;
        public static final String JWK_SET_URL;
        public static final String TOKEN_ENDPOINT_AUTHENTICATION_SIGNING_ALGORITHM;

        private Client() {
        }

        static {
            REQUIRE_PROOF_KEY = CLIENT_SETTINGS_NAMESPACE.concat("require-proof-key");
            REQUIRE_AUTHORIZATION_CONSENT = CLIENT_SETTINGS_NAMESPACE.concat("require-authorization-consent");
            JWK_SET_URL = CLIENT_SETTINGS_NAMESPACE.concat("jwk-set-url");
            TOKEN_ENDPOINT_AUTHENTICATION_SIGNING_ALGORITHM = CLIENT_SETTINGS_NAMESPACE.concat("token-endpoint-authentication-signing-algorithm");
        }
    }
}
