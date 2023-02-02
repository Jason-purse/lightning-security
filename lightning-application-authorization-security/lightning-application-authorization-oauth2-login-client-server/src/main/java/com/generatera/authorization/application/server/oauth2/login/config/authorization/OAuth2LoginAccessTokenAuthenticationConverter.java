package com.generatera.authorization.application.server.oauth2.login.config.authorization;

import com.generatera.authorization.application.server.config.LoginGrantType;
import com.generatera.authorization.application.server.config.token.AuthorizationRequestAuthentication;
import com.generatera.authorization.application.server.config.token.HttpRequestUtil;
import com.generatera.authorization.application.server.config.util.AuthEndPointUtils;
import com.generatera.authorization.server.common.configuration.AuthorizationGrantType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
/**
 * @author FLJ
 * @date 2023/2/1
 * @time 16:41
 * @Description 重定向到已有端点
 *
 *  重定向到{@link OAuth2AuthorizationRequestRedirectFilter} 进行客户端等信息的处理 ...
 *  然后开启授权码流程 ...
 */
public class OAuth2LoginAccessTokenAuthenticationConverter implements OAuth2ClientLoginAccessTokenAuthenticationConverter {

    public static final String DEFAULT_REDIRECT_BASE_URI = OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;

    private String redirectBaseUri = DEFAULT_REDIRECT_BASE_URI;

    public void setRedirectBaseUri(String redirectBaseUri) {
        Assert.hasText(redirectBaseUri,"redirectBaseUri must not be null !!!");
        this.redirectBaseUri = redirectBaseUri;
    }

    @Override
    public Authentication convert(HttpServletRequest request) {
        MultiValueMap<String, String> parameters = HttpRequestUtil.getParameters(request);
        String grant_type = parameters.getFirst("grant_type");
        if (!AuthorizationGrantType.ACCESS_TOKEN.getValue().equalsIgnoreCase(grant_type)) {
            return null;
        }
        String login_grant_type = parameters.getFirst("login_grant_type");
        if (!LoginGrantType.OAUTH2_CLIENT_LOGIN.value().equalsIgnoreCase(login_grant_type)) {
            return null;
        }
        String client_id = parameters.getFirst("client_id");
        String client_secret = parameters.getFirst("client_secret");
        if (!StringUtils.hasText(client_id) || !StringUtils.hasText(client_secret)) {
            AuthEndPointUtils.throwError("invalid_request", "oauth2 authorization_request_param client_id or client_secret must not be null !!!", "");
        }


        String oauth2_grant_type = parameters.getFirst("oauth2_grant_type");

        Map<String, Object> additionalParameters = new HashMap<>();
        parameters.forEach((key, value) -> {
            if (!key.equals("grant_type") && !key.equals("refresh_token") && !key.equals("scope")) {
                additionalParameters.put(key, value.get(0));
            }

        });
        // 开始组装authAccess...Token

        // 不存在默认就是授权码
        if(!StringUtils.hasText(oauth2_grant_type) || org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equalsIgnoreCase(oauth2_grant_type)) {

            String provider = parameters.getFirst("provider");

            // 进行重定向到,我们自身的端点上 ...
            return new AuthorizationRequestAuthentication() {
                @Override
                public void sendRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
                    response.sendRedirect(redirectBaseUri + "/" +provider);
                }
            };
        }

        return getOtherAuthentication(oauth2_grant_type,client_id,client_secret,additionalParameters);
    }

    /**
     * 子类扩展点
     */
    protected  Authentication getOtherAuthentication(String oauth2_grant_type,
                                                    String clientId,String clientSecret,
                                                    Map<String, Object> additionalParameters) {
        return null;
    }
}
