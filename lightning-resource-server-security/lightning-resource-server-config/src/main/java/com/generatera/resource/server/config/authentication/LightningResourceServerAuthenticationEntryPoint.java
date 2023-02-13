package com.generatera.resource.server.config.authentication;

import com.generatera.security.authorization.server.specification.util.AuthHttpResponseUtil;
import com.jianyue.lightning.result.Result;
import com.jianyue.lightning.util.JsonUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author FLJ
 * @date 2023/2/13
 * @time 10:02
 * @Description 资源服务器认证 entryPoint
 *
 *  默认实现
 */
public interface LightningResourceServerAuthenticationEntryPoint extends AuthenticationEntryPoint, AccessDeniedHandler {

    String getInvalidTokenErrorMessage();

    String getAccessDeniedErrorMessage();

    @Override
    default void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if(StringUtils.hasText(getInvalidTokenErrorMessage())) {
            AuthHttpResponseUtil.commence(response,
                    JsonUtil.getDefaultJsonUtil().asJSON(Result.error(HttpStatus.UNAUTHORIZED.value(),getInvalidTokenErrorMessage())));
        }
        else {
            AuthHttpResponseUtil.commence(response,
                    JsonUtil.getDefaultJsonUtil().asJSON(Result.error(HttpStatus.UNAUTHORIZED.value(), "invalid token.")));
        }
    }

    @Override
    default void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if(StringUtils.hasText(getAccessDeniedErrorMessage())) {
            AuthHttpResponseUtil.commence(response,
                    JsonUtil.getDefaultJsonUtil().asJSON(Result.error(HttpStatus.FORBIDDEN.value(), getAccessDeniedErrorMessage())));
        }
        else {
            AuthHttpResponseUtil.commence(response,
                    JsonUtil.getDefaultJsonUtil().asJSON(Result.error(HttpStatus.FORBIDDEN.value(),"access denied.")));
        }
    }
}
