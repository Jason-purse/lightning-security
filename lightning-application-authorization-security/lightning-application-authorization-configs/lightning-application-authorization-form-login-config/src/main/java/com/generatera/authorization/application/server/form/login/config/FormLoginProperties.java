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
        private String loginRequestUrl;

        private String loginSuccessMessage = "LOGIN SUCCESS";

        private String accountExpiredMessage = "ACCOUNT EXPIRED";

        private String accountLockedMessage = "ACCOUNT FORBIDDEN";

        private Boolean enableAccountStatusInform = false;

        private String accountStatusMessage = "ACCOUNT STATUS EXCEPTION";


        private String badCredentialMessage = "BAD CREDENTIAL ERROR";
    }

    @Data
    public static class NoSeparation {

        private String loginProcessUrl;

        private String loginPageUrl;

        private String successForwardUrl;

        private String failureForwardUrl;

    }

}
