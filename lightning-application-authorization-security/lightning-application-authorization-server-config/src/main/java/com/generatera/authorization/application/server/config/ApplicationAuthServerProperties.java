package com.generatera.authorization.application.server.config;

import com.generatera.authorization.server.common.configuration.provider.metadata.oidc.OidcProviderConfigurationEndpointFilter;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettingProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.generatera.authorization.application.server.config.ApplicationAuthServerProperties.APPLICATION_AUTH_SERVER_PREFIX;

/**
 * 应用级别的 认证属性
 */
@Data
@ConfigurationProperties(prefix = APPLICATION_AUTH_SERVER_PREFIX)
public class ApplicationAuthServerProperties {

    public static final String APPLICATION_AUTH_SERVER_PREFIX = "lightning.app.auth.server.config";

    private ServerMetaDataEndpointConfig serverMetaDataEndpointConfig = new ServerMetaDataEndpointConfig();
    /**
     * 是否前后端分离
     */
    private Boolean isSeparation = false;

    private final BackendSeparation backendSeparation = new BackendSeparation();

    private final NoSeparation noSeparation = new NoSeparation();

    /**
     * 提供者配置属性
     * 为元数据端点提供属性 为资源服务器 交换公钥 提供机会 ...
     */
    private final ProviderSettingProperties providerSettingProperties = new ProviderSettingProperties();

    /**
     * 可以自定义的前缀 ...
     */
    public String appAuthPrefix = AppAuthConfigConstant.APP_AUTH_SERVER_PREFIX;


    @Data
    public static class ServerMetaDataEndpointConfig {

        public static final String ENABLE_OIDC = APPLICATION_AUTH_SERVER_PREFIX + ".serverMetaDataEndpointConfig.enableOidc";

        public static final String OPEN_CONNECT_ID_METADATA_ENDPOINT = OidcProviderConfigurationEndpointFilter.DEFAULT_OIDC_PROVIDER_CONFIGURATION_ENDPOINT_URI;

        /**
         * 那么默认需要 ..
         */
        private Boolean enableOidc = true;


    }

    @Data
    public static class BackendSeparation {

        private String logoutProcessUrl = "/logout/process";
        private String logoutSuccessMessage = "LOGOUT SUCCESS";


        /**
         * 它主要控制,以下两个选项 ..
         */
        private Boolean enableAccountStatusDetails = false;

        private String accountExpiredMessage = "ACCOUNT EXPIRED";

        private String accountLockedMessage = "ACCOUNT FORBIDDEN";




        /**
         * 主要管理 账户异常信息(accountStatusMessage, 以及 badCredentialMessage),以及 enableAccountStatusDetails的控制能力 ..
         * 主要以下四项 ..
         */
        private Boolean enableAuthFailureDetails = true;

        private String accountStatusMessage = "ACCOUNT STATUS EXCEPTION";


        private String badCredentialMessage = "BAD CREDENTIAL ERROR";

        private String loginSuccessMessage = "LOGIN SUCCESS";

        private String loginFailureMessage = "LOGIN FAILURE";

        /**
         * 未登录提示
         */
        private String unAuthenticatedMessage;

    }



    /**
     * 登出的配置 ...
     */
    @Data
    public static class NoSeparation {

        /**
         * 登出页面 url
         */
        private String logoutPageUrl = "/logout";

        /**
         * 可以自定义(默认等价于 logoutPageUrl)
         *
         * 不指定,默认等价于 logoutPageUrl,但是登出处理方式是POST,
         * 如果需要完全自定义，覆盖登出处理器 ...
         */
        private String logoutProcessUrl;

        private String logoutSuccessUrl = "/login?logout";

        private String loginPageUrl = "/login";


        // ------------------------------- 以下 url 将不会自动拼接 app 授权服务器前缀 -----------------------------
        /**
         * 仅当开启了 enableSavedRequestForward 才有效
         */
        private String defaultSuccessUrl;

        /**
         * enableForward 则需要是一个具体的资源
         *
         * 如果填写,则将强制重定向到 此路径上 ...
         * enableSavedRequestForward 选项将失效 ...
         */
        private String successForwardOrRedirectUrl;

        /**
         *  enableForward 则需要是一个具体的资源
         */
        private String failureForwardOrRedirectUrl = AppAuthConfigConstant.APP_AUTH_SERVER_PREFIX + "/login?error";

        /**
         * 默认是转发,如果不是就是重定向
         */
        private Boolean enableForward = false;


        /**
         * 开启了它,与 enableForward属性互斥 ..(则后者无效)
         *
         * 基于使用场景开启它:
         * 主要它是基于jvm 内存存储发起的请求 ..
         *
         * 当然前后端分离的时候,直接 关联前后端地址即可 ..
         * (例如向授权中心前端发起请求,然后记录它所请求地址,然后转向登陆页面,登陆成功之后跳转回来请求之前的地址 ) ..
         * 当然这样很繁琐  / 可能也很蠢
         *
         * 所以这个选项默认开启 ..(只不过基于http session 缓存之前的请求) ...
         * 但是一般授权中心是集群,这种流量产生的消耗也只是其中的一部分 ..
         *
         *
         * todo()
         * 没有自动定时清理 session记录的发起请求信息, 应该改进为使用redis存储可能更好 ..
         */
        private Boolean enableSavedRequestForward = true;

    }


}
