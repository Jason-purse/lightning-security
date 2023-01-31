package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties;
import com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties.StoreKind;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;

import static com.generatera.authorization.application.server.oauth2.login.config.OAuth2LoginProperties.OAUTH2_LOGIN_AUTH_SERVER_PREFIX;

@Data
@ConfigurationProperties(prefix = OAUTH2_LOGIN_AUTH_SERVER_PREFIX)
public class OAuth2LoginProperties {
    public static final String OAUTH2_LOGIN_AUTH_SERVER_PREFIX = "lightning.auth.app.server.oauth2.login.config";

    /**
     * 可能存在多个,所以使用 "*"
     */
    private String loginProcessUrl = "/auth/v1/oauth2/login/code/*";

    private BackendSeparation backendSeparation = new BackendSeparation();

    private NoSeparation noSeparation = new NoSeparation();

    /**
     * 客户端身份仓库存储方式,不同的存储方式,对clientRegistration 的要求也不同 ..
     */
    private StoreKind clientRegistrationStoreKind = StoreKind.MEMORY;

    /**
     * 授权请求端点,主要是保存授权请求的方式配置 ..
     */
    private OAuthorizationRequestEndpoint authorizationRequestEndpoint = new OAuthorizationRequestEndpoint();

    /**
     * 重定向端点,主要是配置重定向 到 授权请求开始的url上的配置 ..
     *
     * 用户或者应用发起认证请求 -> 根据重定向 过滤器的处理器 -> 重定向到真正的 授权请求开始处理的过滤器上 ...
     *
     * 也就是说重定向 端点会根据你请求(作为那种oauth2 客户端)来形成有效的 授权请求,从而开始授权请求flow ..
     */
    private RedirectionEndpoint redirectionEndpoint = new RedirectionEndpoint();

    /**
     * 授权的客户端存储方式
     * 也就是当客户端代表用户进行 oauth2 授权之后,那么这个客户端可以看作是认证过后的客户端 ..
     * 我们可以存储用作后续代表用户进行 oauth2 授权中心资源的获取 ...(例如通过访问token 获取一定量的资源,或者通过刷新token 去重新获取
     * 一个访问 token) ...
     */
    private StoreKind authorizedClientStoreKind = StoreKind.MEMORY;

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
        private String loginPageUrl;

        /**
         * 仅当启用了 enableSavedRequestForward 才有效果
         */
        private String defaultSuccessUrl;

        /**
         * 仅当开启了 enableForward 才有效果
         */
        private String successUrl;

        /**
         * 仅当开启了 enableForward 才有效果
         */
        private String failureUrl;


        /**
         * 默认开启转发
         */
        private Boolean enableForward = true;

        /**
         * 此选项和 enableForward 互斥
         */
        private Boolean enableSavedRequestForward = true;

    }



}
