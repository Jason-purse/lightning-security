package com.generatera.authorization.application.server.oauth2.login.config.token;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    private final ClientRegistration clientRegistration;

    private final HttpServletRequest request;

    private final HttpServletResponse response;
    public PasswordGrantAuthorizationRequestAuthentication(
            String oauth2GrantType, String clientId, String clientSecret, Map<String, Object> additionalParameters,
            HttpServletRequest request,
            HttpServletResponse response) {
        super(oauth2GrantType, clientId, clientSecret, additionalParameters);
        username = additionalParameters.remove(OAuth2ParameterNames.USERNAME).toString();
        password = additionalParameters.remove(OAuth2ParameterNames.PASSWORD).toString();
        clientRegistration = ((ClientRegistration) additionalParameters.remove("clientInfo"));
        this.request =request;
        this.response = response;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public ClientRegistration getClientRegistration() {
        return clientRegistration;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }
}
