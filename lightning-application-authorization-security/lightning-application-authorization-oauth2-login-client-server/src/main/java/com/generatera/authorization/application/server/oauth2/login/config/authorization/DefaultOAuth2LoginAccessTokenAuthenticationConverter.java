package com.generatera.authorization.application.server.oauth2.login.config.authorization;

import com.generatera.authorization.application.server.config.LoginGrantType;
import com.generatera.authorization.application.server.config.token.AuthorizationRequestAuthentication;
import com.generatera.authorization.application.server.config.token.HttpRequestUtil;
import com.generatera.authorization.application.server.config.util.AuthEndPointUtils;
import com.generatera.authorization.application.server.oauth2.login.config.client.register.LightningOAuth2ClientRegistrationRepository;
import com.generatera.authorization.server.common.configuration.LightningAuthorizationGrantType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
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
 *
 *  子类可以扩展,例如支持{@link org.springframework.security.oauth2.core.AuthorizationGrantType#PASSWORD} 授权形式 ...
 */
public class DefaultOAuth2LoginAccessTokenAuthenticationConverter implements OAuth2ClientLoginAccessTokenAuthenticationConverter {

    public static final String DEFAULT_REDIRECT_BASE_URI = OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;

    private String redirectBaseUri = DEFAULT_REDIRECT_BASE_URI;

    protected final LightningOAuth2ClientRegistrationRepository clientRegistrationRepository;

    public DefaultOAuth2LoginAccessTokenAuthenticationConverter(LightningOAuth2ClientRegistrationRepository clientRegistrationRepository) {
        Assert.notNull(clientRegistrationRepository,"client registration repository must not be null !!!");
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    public void setRedirectBaseUri(String redirectBaseUri) {
        Assert.hasText(redirectBaseUri,"redirectBaseUri must not be null !!!");
        this.redirectBaseUri = redirectBaseUri;
    }

    @Override
    public Authentication convert(HttpServletRequest request,HttpServletResponse response) {
        MultiValueMap<String, String> parameters = HttpRequestUtil.getParameters(request);
        String grant_type = parameters.getFirst("grant_type");
        if (!LightningAuthorizationGrantType.ACCESS_TOKEN.getValue().equalsIgnoreCase(grant_type)) {
            return null;
        }
        String login_grant_type = parameters.getFirst("login_grant_type");
        if (!LoginGrantType.OAUTH2_CLIENT_LOGIN.value().equalsIgnoreCase(login_grant_type)) {
            return null;
        }

        String oauth2_grant_type = parameters.getFirst("oauth2_grant_type");

        String provider = parameters.getFirst("provider");
        if (!StringUtils.hasText(provider) ) {
            AuthEndPointUtils.throwError("invalid_request", "oauth2 provider must not be null !!!", "");
        }

        Map<String, Object> additionalParameters = new HashMap<>();
        parameters.forEach((key, value) -> {
            if (!key.equals("grant_type") && !key.equals("refresh_token") && !key.equals("scope") && !key.equals("oauth2_grant_type") &&
            !key.equals("login_grant_type") && !key.equals("provider")) {
                additionalParameters.put(key, value.get(0));
            }
        });


        assert provider != null;
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(provider.trim());
        if(clientRegistration == null) {
            AuthEndPointUtils.throwError("invalid_request", "oauth2 provider is  invalid !!!", "");
        }

        // 开始组装authAccess...Token
        // 不存在默认就是授权码
        if(!StringUtils.hasText(oauth2_grant_type) || org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equalsIgnoreCase(oauth2_grant_type)) {
            // 进行重定向到,我们自身的端点上 ...
            return new AuthorizationRequestAuthentication() {
                @Override
                public void sendRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
                    response.sendRedirect(redirectBaseUri + "/" + provider);
                }

                @Override
                public boolean needRedirect() {
                    return true;
                }
            };
        }

        // 否则是其他的 ...

        return getOtherAuthentication(oauth2_grant_type,clientRegistration,additionalParameters,request,response);
    }

    /**
     * 子类扩展点
     */
    protected  Authentication getOtherAuthentication(String oauth2_grant_type,
                                                    ClientRegistration clientRegistration,
                                                    Map<String, Object> additionalParameters,
                                                     HttpServletRequest request,HttpServletResponse response) {
        return null;
    }
}
