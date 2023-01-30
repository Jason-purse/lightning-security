package com.generatera.authorization.application.server.form.login.config;

import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = FormLoginProperties.PROPERTIES_PREFIX)
public class FormLoginProperties {
    public static final String PROPERTIES_PREFIX = ApplicationAuthServerProperties.APPLICATION_AUTH_SERVER_PREFIX + ".form.login.config";
    public static final String IS_SEPARATION = PROPERTIES_PREFIX + ".isSeparation";

    private String usernameParameterName = "username";

    private String passwordParameterName = "password";

    /**
     * 表单的登录url (将自动拼接 app auth server 前缀) ...
     */
    private String loginProcessUrl = "/form/login/process";

    private final NoSeparation noSeparation = new NoSeparation();


    private final BackendSeparation backendSeparation = new BackendSeparation();



    @Data
    public static class NoSeparation {

        /**
         * 可以指定自己的登录页面
         * 将自动拼接 app auth server 前缀
         *
         * 可以对 {@link ApplicationAuthServerProperties}的同名属性进行覆盖 ..
         */
        private String loginPageUrl;
    }

    @Data
    public static class BackendSeparation {

        /**
         * 是否启用默认登录页面
         */
        private boolean enableLoginPage;
    }


}
