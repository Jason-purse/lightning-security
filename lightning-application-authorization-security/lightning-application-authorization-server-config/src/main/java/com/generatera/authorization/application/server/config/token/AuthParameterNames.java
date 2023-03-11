package com.generatera.authorization.application.server.config.token;

/**
 * 常用认证参数
 */
public interface AuthParameterNames {
    String USERNAME = "username";
    String PASSWORD = "password";
    /**
     * 登陆授权方式
     * @see com.generatera.authorization.application.server.config.LoginGrantType
     */
    String LOGIN_GRANT_TYPE = "login_grant_type";

    /**
     * 授权类型
     * @see com.generatera.authorization.server.common.configuration.LightningAuthorizationGrantType
     */
    String GRANT_TYPE = "grant_type";
}
