package com.generatera.security.authorization.server.specification.components.authentication;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 实现 认证失败的处理,并实现认证成功的处理 ..
 *
 * 将 entrypoint的commence方法代理到 onAuthenticationFailure ..
 * 同时 {@link AuthenticationSuccessHandler} 需要子类继承 ..
 */
public interface LightningAuthenticationEntryPoint extends AuthenticationEntryPoint , AuthenticationSuccessHandler, AuthenticationFailureHandler {
    @Override
    default void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        onAuthenticationFailure(request,response,authException);
    }

}
