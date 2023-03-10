package com.generatera.authorization.application.server.oauth2.login.config.authorization;

import com.generatera.authorization.application.server.config.util.AuthEndPointUtils;
import com.generatera.authorization.application.server.oauth2.login.config.token.DefaultAuthorizationRequestAuthentication;
import com.generatera.authorization.server.common.configuration.AuthorizationGrantType;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author FLJ
 * @date 2023/3/10
 * @time 10:17
 * @Description 使用username / password 形式来直接登录 !!!
 */
public class PasswordGrantAccessTokenAuthenticationConverter extends OAuth2LoginAccessTokenAuthenticationConverter {

    public PasswordGrantAccessTokenAuthenticationConverter(ClientRegistrationRepository clientRegistrationRepository) {
        super(clientRegistrationRepository);
    }

    @Override
    protected Authentication getOtherAuthentication(String oauth2_grant_type, ClientRegistration clientRegistration,Map<String, Object> additionalParameters, HttpServletRequest request) {
        // 完整提供用于处理
        if(AuthorizationGrantType.PASSWORD.getValue().equals(oauth2_grant_type)) {

            Object username = additionalParameters.get(OAuth2ParameterNames.USERNAME);
            ElvisUtil.isEmptyConsumer(username, () -> {
                AuthEndPointUtils.throwError("invalid request","username must not be null !!!","");
            });

            Object password = additionalParameters.get(OAuth2ParameterNames.PASSWORD);
            ElvisUtil.isEmptyConsumer(username, () -> {
                AuthEndPointUtils.throwError("invalid request","password must not be null !!!","");
            });

            // 验证客户端id / 密码 ..
            return new DefaultAuthorizationRequestAuthentication(
                    oauth2_grant_type,
                    clientRegistration.getClientId(),
                    clientRegistration.getClientSecret(),
                    // 包含 用户名和密码
                    additionalParameters
            );
        }
        return super.getOtherAuthentication(oauth2_grant_type, clientRegistration,additionalParameters, request);
    }
}
