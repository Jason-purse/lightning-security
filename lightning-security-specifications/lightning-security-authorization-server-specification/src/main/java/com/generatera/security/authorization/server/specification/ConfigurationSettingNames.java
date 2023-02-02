package com.generatera.security.authorization.server.specification;
/**
 * @author FLJ
 * @date 2023/1/16
 * @time 16:41
 * @Description 配置设置的名称, spring-oauth2 -copys
 *
 * 包括Token的一些基本配置名称 ..
 * 以及提供者的一些基本配置名称 ..
 */
public final class ConfigurationSettingNames {
    private static final String SETTINGS_NAMESPACE = "settings.";

    private ConfigurationSettingNames() {
    }

    public static final class Token {
        private static final String TOKEN_SETTINGS_NAMESPACE = SETTINGS_NAMESPACE.concat("token.");
        public static final String ACCESS_TOKEN_TIME_TO_LIVE;
        /**
         * access token 颁发格式
         */
        public static final String ACCESS_TOKEN_FORMAT;

        public static final String ACCESS_TOKEN_VALUE_TYPE;

        public static final String ACCESS_TOKEN_VALUE_FORMAT;

        public static final String REFRESH_TOKEN_VALUE_FORMAT;
        public static final String REUSE_REFRESH_TOKEN;
        public static final String REFRESH_TOKEN_TIME_TO_LIVE;
        public static final String REFRESH_TOKEN_VALUE_TYPE;
        public static final String AUDIENCE;

        public static final String GRANT_TYPES;


        private Token() {
        }

        static {
            ACCESS_TOKEN_TIME_TO_LIVE = TOKEN_SETTINGS_NAMESPACE.concat("access-token-time-to-live");
            ACCESS_TOKEN_FORMAT = TOKEN_SETTINGS_NAMESPACE.concat("access-token-format");
            REUSE_REFRESH_TOKEN = TOKEN_SETTINGS_NAMESPACE.concat("reuse-refresh-token");
            REFRESH_TOKEN_TIME_TO_LIVE = TOKEN_SETTINGS_NAMESPACE.concat("refresh-token-time-to-live");
            AUDIENCE = TOKEN_SETTINGS_NAMESPACE.concat("token-audience");
            ACCESS_TOKEN_VALUE_TYPE = TOKEN_SETTINGS_NAMESPACE.concat("access-token-value-type");
            ACCESS_TOKEN_VALUE_FORMAT = TOKEN_SETTINGS_NAMESPACE.concat("access-token-value-format");
            REFRESH_TOKEN_VALUE_FORMAT = TOKEN_SETTINGS_NAMESPACE.concat("refresh-token-value-format");
            REFRESH_TOKEN_VALUE_TYPE = TOKEN_SETTINGS_NAMESPACE.concat("refresh-token-value-type");
            GRANT_TYPES = TOKEN_SETTINGS_NAMESPACE.concat("grant_types");
        }
    }

    public static final class Provider {
        private static final String PROVIDER_SETTINGS_NAMESPACE = SETTINGS_NAMESPACE.concat("provider.");
        public static final String ISSUER;
        public static final String JWK_SET_ENDPOINT;
        public static final String TOKEN_REVOCATION_ENDPOINT;
        public static final String TOKEN_INTROSPECTION_ENDPOINT;
        public static final String TOKEN_ENDPOINT;

        private Provider() {
        }

        static {
            ISSUER = PROVIDER_SETTINGS_NAMESPACE.concat("issuer");
            JWK_SET_ENDPOINT = PROVIDER_SETTINGS_NAMESPACE.concat("jwk-set-endpoint");
            TOKEN_ENDPOINT = PROVIDER_SETTINGS_NAMESPACE.concat("token-endpoint");
            TOKEN_REVOCATION_ENDPOINT = PROVIDER_SETTINGS_NAMESPACE.concat("token-revocation-endpoint");
            TOKEN_INTROSPECTION_ENDPOINT = PROVIDER_SETTINGS_NAMESPACE.concat("token-introspection-endpoint");
        }
    }

}
