package com.generatera.resource.server.config.token;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author FLJ
 * @date 2023/1/5
 * @time 13:25
 * @Description authentication entry point ext
 */
public interface LightningAuthenticationEntryPoint extends AuthenticationEntryPoint, AuthenticationFailureHandler {
    @Override
    default void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        onAuthenticationFailure(request, response, authException);
    }
}
