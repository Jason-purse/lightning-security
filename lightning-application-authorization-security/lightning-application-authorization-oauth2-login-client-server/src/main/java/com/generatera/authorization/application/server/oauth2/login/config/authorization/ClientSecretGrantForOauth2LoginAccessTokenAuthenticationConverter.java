package com.generatera.authorization.application.server.oauth2.login.config.authorization;

import com.generatera.authorization.application.server.oauth2.login.config.token.DefaultAuthorizationRequestAuthentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
/**
 * @author FLJ
 * @date 2023/3/7
 * @time 12:57
 * @Description 支持
 */
public class ClientSecretGrantForOauth2LoginAccessTokenAuthenticationConverter extends OAuth2LoginAccessTokenAuthenticationConverter {

    @Override
    protected Authentication getOtherAuthentication(String oauth2_grant_type, String clientId, String clientSecret, Map<String, Object> additionalParameters,HttpServletRequest request) {
        // 判断它的oauth_grant_type
        if(AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equals(oauth2_grant_type)) {
            return new DefaultAuthorizationRequestAuthentication(
                    oauth2_grant_type,
                    clientId,
                    clientSecret,
                    additionalParameters
            );
        }

        return super.getOtherAuthentication(oauth2_grant_type, clientId, clientSecret, additionalParameters,request);
    }
}
