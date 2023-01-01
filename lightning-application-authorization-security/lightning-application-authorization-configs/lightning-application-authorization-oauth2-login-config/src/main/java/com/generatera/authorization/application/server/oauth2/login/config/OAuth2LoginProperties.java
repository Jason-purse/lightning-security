package com.generatera.authorization.application.server.oauth2.login.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;

@Data
@ConfigurationProperties(prefix = "lightning.auth.app.server.oauth2.login.config")
public class OAuth2LoginProperties {


    private Boolean isSeparation = false;

    private String loginProcessUrl;

    private BackendSeparation backendSeparation = new BackendSeparation();

    private NoSeparation noSeparation = new NoSeparation();


    private StoreKind clientRegistrationStoreKind = StoreKind.IN_MEMORY;

    private OAuthorizationRequestEndpoint authorizationRequestEndpoint = new OAuthorizationRequestEndpoint();

    private RedirectionEndpoint redirectionEndpoint = new RedirectionEndpoint();

    private StoreKind authorizedClientStoreKind = StoreKind.IN_MEMORY;

    @Data
    public static class OAuthorizationRequestEndpoint {

        private final static Long DEFAULT_STORE_TIME_OF_MILLIS = 5 * 1000 * 60L;
        private String authorizationRequestBaseUri;

        private AuthorizationRequestStoreKind storeKind = AuthorizationRequestStoreKind.IN_MEMORY;

        private Long storeTimeOfMillis = DEFAULT_STORE_TIME_OF_MILLIS;

        private Redis redis = new Redis();


        public Long getStoreTimeOfMillis() {
            return storeTimeOfMillis - 5 * 1000;
        }

        public enum AuthorizationRequestStoreKind {
            JPA,
            MONGO,
            IN_MEMORY,
            REDIS
        }

        @Data
        public static class Redis {
            private final static String DEFAULT_KEY_PREFIX = "lightning-app-authorization-request-";

            private String keyPrefix = DEFAULT_KEY_PREFIX;
        }
    }

    @Data
    public static class RedirectionEndpoint {
        private final static String DEFAULT_BASE_URL = OAuth2LoginAuthenticationFilter.DEFAULT_FILTER_PROCESSES_URI;
        private String baseUrl = DEFAULT_BASE_URL;
    }


    @Data
    public static class BackendSeparation {

        private String loginSuccessMessage;

        private String loginFailureMessage;

        private Boolean enableAuthErrorDetail = false;

    }

    @Data
    public static class NoSeparation {
        private String loginPageUrl = DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL;

        private String defaultSuccessUrl = "/";

        private String defaultFailureUrl;

    }

    public enum StoreKind {
        JPA,
        MONGO,
        IN_MEMORY
    }


}
