package com.generatera.authorization.server.common.configuration;

import com.generatera.security.authorization.server.specification.TokenIssueFormat;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "lightning.auth.server.common.component")
public class AuthorizationServerComponentProperties {

    public static final String TOKEN_GENERATOR_NAME = "lightning.authentication.token.generator";

    private final ProviderSettingProperties providerSettingProperties = new ProviderSettingProperties();

    private final TokenSettings tokenSettings = new TokenSettings();

    @Data
    public static class TokenSettings {
        public static Long DEFAULT_ACCESS_TOKEN_TIME_TO_LIVE = 30 * 60 * 1000L;
        public static Long DEFAULT_REFRESH_TOKEN_TIME_TO_LIVE = 7 * 24 * 60 * 60 * 1000L;

        public static String[] DEFAULT_AUDIENCES = new String[]{"*"};

        private Long accessTokenTimeToLive = DEFAULT_ACCESS_TOKEN_TIME_TO_LIVE;

        private TokenIssueFormat tokenFormat = TokenIssueFormat.SELF_CONTAINED;

        private Boolean reuseRefreshToken = Boolean.TRUE;

        private Long refreshTokenTimeToLive = DEFAULT_REFRESH_TOKEN_TIME_TO_LIVE;

        private String[] audiences = DEFAULT_AUDIENCES;

        /**
         * 默认自省Token
         */
        private Boolean isPlain = Boolean.TRUE;


        public Long getAccessTokenTimeToLive() {
            return accessTokenTimeToLive == null ? DEFAULT_ACCESS_TOKEN_TIME_TO_LIVE : accessTokenTimeToLive - 5 * 1000L;
        }

        public Long getRefreshTokenTimeToLive() {
            return refreshTokenTimeToLive == null ? DEFAULT_REFRESH_TOKEN_TIME_TO_LIVE : refreshTokenTimeToLive - 5 * 1000L;
        }

        public Boolean getReuseRefreshToken() {
            return reuseRefreshToken != null ? reuseRefreshToken : Boolean.FALSE;
        }

        public Boolean getIsPlain() {
            return isPlain != null ? isPlain : Boolean.FALSE;
        }
    }




    @Data
    public static class ProviderSettingProperties {

        // 可以为空(自己自动生成)
        private String issuer;

        private String authorizationEndpoint = "/auth/v1/oauth2/authorize";

        private String tokenEndpoint = "/auth/v1/oauth2/token";

        private String jwkSetEndpoint = "/auth/v1/oauth2/jwks";

        private String tokenRevocationEndpoint = "/auth/v1/oauth2/revoke";

        private String tokenIntrospectionEndpoint = "/auth/v1/oauth2/introspect";

        private String oidcClientRegistrationEndpoint = "/auth/v1/connect/register";

        private String oidcUserInfoEndpoint = "/auth/v1/userinfo";
    }

}
