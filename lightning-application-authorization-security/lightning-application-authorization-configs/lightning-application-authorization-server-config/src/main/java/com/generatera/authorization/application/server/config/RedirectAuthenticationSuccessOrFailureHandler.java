package com.generatera.authorization.application.server.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 重定向 成功或者失败 处理器 ..
 */
public class RedirectAuthenticationSuccessOrFailureHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

    private final String redirectUrl;

    public RedirectAuthenticationSuccessOrFailureHandler(String redirectUrl) {
        Assert.isTrue(UrlUtils.isValidRedirectUrl(redirectUrl), () -> {
            return "'" + redirectUrl + "' is not a valid forward URL";
        });
        this.redirectUrl = redirectUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.sendRedirect(redirectUrl);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.sendRedirect(redirectUrl);
    }
}
