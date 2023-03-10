package com.generatera.authorization.application.server.oauth2.login.config.token;

import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import java.util.Map;
/**
 * @author FLJ
 * @date 2023/3/10
 * @time 10:31
 * @Description 能够直接获取用户名和密码而已 ..
 */
public class PasswordGrantAuthorizationRequestAuthentication extends DefaultAuthorizationRequestAuthentication {

    private final String username;

    private final String password;
    public PasswordGrantAuthorizationRequestAuthentication(String oauth2GrantType, String clientId, String clientSecret, Map<String, Object> additionalParameters) {
        super(oauth2GrantType, clientId, clientSecret, additionalParameters);
        username = additionalParameters.remove(OAuth2ParameterNames.USERNAME).toString();
        password = additionalParameters.remove(OAuth2ParameterNames.PASSWORD).toString();
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
}
