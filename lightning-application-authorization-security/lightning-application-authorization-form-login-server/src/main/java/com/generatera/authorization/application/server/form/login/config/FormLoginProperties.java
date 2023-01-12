package com.generatera.authorization.application.server.form.login.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = FormLoginProperties.PROPERTIES_PREFIX)
public class FormLoginProperties {
    public static final String PROPERTIES_PREFIX = "lightning.auth.app.server.form.login.config";
    public static final String IS_SEPARATION = PROPERTIES_PREFIX + ".isSeparation";
    /**
     * 与后端分离
     */
    private Boolean isSeparation = Boolean.FALSE;

    private String loginPageUrl;

    private String loginProcessUrl;

    private String usernameParameterName = "username";

    private String passwordParameterName = "password";


    private BackendSeparation backendSeparation = new BackendSeparation();


    private NoSeparation noSeparation = new NoSeparation();







    @Data
    public static class BackendSeparation {

        private String loginSuccessMessage = "LOGIN SUCCESS";

        private String loginFailureMessage = "LOGIN FAILURE";

        private String accountExpiredMessage = "ACCOUNT EXPIRED";

        private String accountLockedMessage = "ACCOUNT FORBIDDEN";

        private Boolean enableAccountStatusInform = false;

        private String accountStatusMessage = "ACCOUNT STATUS EXCEPTION";


        private String badCredentialMessage = "BAD CREDENTIAL ERROR";

        private final TokenSettings tokenSettings = new TokenSettings();

        /**
         * 默认不作为 资源服务器 ...
         */
        private Boolean asResourceServer = Boolean.FALSE;

        // 分离的情况下,需要考虑 Token 的解析 ...
        // 当需要开启Token的解析时,我们需要引入对应的resource server 依赖 ....
    }

    @Data
    public static class NoSeparation {

        private String loginPageUrl;

        /**
         * 仅当开启了 enableSavedRequestForward 才有效
         */
        private String defaultSuccessUrl;

        /**
         * 仅当开启了 enableForward才有效
         */
        private String successForwardOrRedirectUrl;

        /**
         * 仅当开启了 enableForward才有效
         */
        private String failureForwardOrRedirectUrl;

        /**
         * 默认是转发,如果不是就是重定向
         */
        private Boolean enableForward = true;


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

    @Data
    public static class TokenSettings {
        /**
         * 简单生成,就是生成一个唯一字符串即可 - 映射 用户信息
         * 否则直接将 用户信息放入jwt token中 ..
         */
        private Boolean accessTokenIsPlain;

        private Long accessTokenExpiredDuration = 30 * 1000 * 60L;

        /**
         * 刷新Token 保留一周 时间
         */
        private Long refreshTokenExpiredDuration = 7 * 24 * 60 * 60 * 1000L;

        /**
         * 前端地址 ..
         */
        private String frontEndUrl;


        public Boolean isPlain() {
            return accessTokenIsPlain;
        }

        public Long getAccessTokenExpiredDuration() {
            return accessTokenExpiredDuration - 5 * 1000L;
        }

        public Long getRefreshTokenExpiredDuration() {
            return refreshTokenExpiredDuration - 5 * 1000L;
        }


    }


}
