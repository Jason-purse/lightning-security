package com.generatera.central.oauth2.authorization.server.configuration.components.authentication;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author FLJ
 * @date 2023/2/3
 * @time 13:48
 * @Description 主要负责 认证凭证的获取,并且包含能够感知 授权码流程 !!!
 */
public class DefaultOAuth2CentralServerAuthenticationSuccessHandler implements LightningOAuth2CentralServerAuthenticationSuccessHandler {

    /**
     * 授权请求重定向 url 地址 ...
     */
    private String authorizationCodeRequestFlowUrlAttribute = "AUTHORIZATION_CODE_REQUEST_FLOW_REDIRECT_URL";

    private  AuthenticationSuccessHandler authenticationSuccessHandler = new SavedRequestAwareAuthenticationSuccessHandler();

    public DefaultOAuth2CentralServerAuthenticationSuccessHandler() {

    }

    public DefaultOAuth2CentralServerAuthenticationSuccessHandler(AuthenticationSuccessHandler authenticationSuccessHandler) {
        Assert.notNull(authenticationSuccessHandler,"authenticationSuccessHandler must not be null !!!");
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    public void setAuthorizationCodeRequestFlowUrlAttribute(String authorizationCodeRequestFlowUrlAttribute) {
        Assert.notNull(authorizationCodeRequestFlowUrlAttribute, "authorizationCodeRequestFlowUrlAttribute must not be null !!!");
        this.authorizationCodeRequestFlowUrlAttribute = authorizationCodeRequestFlowUrlAttribute;
    }

    public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler authenticationSuccessHandler) {
        Assert.notNull(authenticationSuccessHandler,"authenticationSuccessHandler must not be null !!!");
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 必要时创建session
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object sessionAttribute = session.getAttribute(authorizationCodeRequestFlowUrlAttribute);
            if (sessionAttribute != null) {
                response.sendRedirect(sessionAttribute.toString());
                return;
            }
        }

        authenticationSuccessHandler.onAuthenticationSuccess(request,response,authentication);
    }
}
