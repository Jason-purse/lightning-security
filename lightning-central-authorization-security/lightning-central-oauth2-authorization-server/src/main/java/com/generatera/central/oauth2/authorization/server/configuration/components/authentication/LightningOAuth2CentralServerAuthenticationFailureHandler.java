package com.generatera.central.oauth2.authorization.server.configuration.components.authentication;

import com.generatera.central.oauth2.authorization.server.configuration.exception.InternalOAuth2AuthenticationException;
import com.generatera.security.authorization.server.specification.util.AuthHttpResponseUtil;
import com.jianyue.lightning.util.JsonUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author Sun.
 */
public interface LightningOAuth2CentralServerAuthenticationFailureHandler extends AuthenticationFailureHandler {
    @Override
    default void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        if (exception instanceof OAuth2AuthenticationException e) {
            if(exception instanceof InternalOAuth2AuthenticationException internalOAuth2AuthenticationException) {
                this.onAuthenticationFailure(request,response, ((AuthenticationException) internalOAuth2AuthenticationException.getCause()));
                return ;
            }

            AuthHttpResponseUtil.commence(response, JsonUtil.getDefaultJsonUtil().asJSON(
                    Map.of("error",e.getError().getErrorCode(),
                            "error_description",e.getError().getDescription())
            ));
        }
        else {
            // 异常有什么写什么
            AuthHttpResponseUtil.commence(response,
                    JsonUtil.getDefaultJsonUtil().asJSON(
                            Map.of("error","invalid request",
                                    "error_description",exception.getMessage())
                    ));
        }
    }
}
