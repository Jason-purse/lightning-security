package com.generatera.authorization.application.server.form.login.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "lightning.auth.app.server.form.login.config")
public class FormLoginProperties {
    /**
     * 与后端分离
     */
    private Boolean isSeparation;

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
    }

    @Data
    public static class NoSeparation {

        private String loginPageUrl;

        private String successForwardUrl;

        private String failureForwardUrl;


        /**
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
