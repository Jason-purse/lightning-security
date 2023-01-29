package com.generatera.authorization.application.server.config.authentication;

import com.generatera.authorization.application.server.config.token.AuthAccessTokenAuthenticationToken;
import com.generatera.security.authorization.server.specification.components.authentication.LightningAuthenticationEntryPoint;
import com.generatera.security.authorization.server.specification.util.AuthHttpResponseUtil;
import com.jianyue.lightning.result.Result;
import com.jianyue.lightning.util.JsonUtil;
import org.springframework.security.core.Authentication;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 抽象认证 entry point
 */
public interface LightningAbstractAuthenticationEntryPoint extends LightningAuthenticationEntryPoint {

    String getLoginSuccessMessage();

    @Override
    default void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        AuthHttpResponseUtil.commence(
                response,
                JsonUtil.getDefaultJsonUtil().asJSON(
                        Result.success(200, getLoginSuccessMessage(),
                                ((AuthAccessTokenAuthenticationToken) authentication))
                )
        );
    }
}
