package com.generatera.authorization.application.server.oauth2.login.config.authentication;

import com.generatera.authorization.application.server.config.ApplicationAuthException;
import com.generatera.authorization.application.server.config.AuthHttpResponseUtil;
import com.generatera.authorization.application.server.config.token.ApplicationLevelAuthorizationToken;
import com.jianyue.lightning.result.Result;
import com.jianyue.lightning.util.JsonUtil;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LightningOAuth2LoginAuthenticationEntryPoint implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

    @Setter
    private String loginSuccessMessage = "LOGIN_SUCCESS";

    /**
     * 一般开发阶段才需要此标识
     */
    @Setter
    private Boolean enableAuthErrorDetails = false;

    @Setter
    private String authErrorMessage = "LOGIN_FAILURE";


    public LightningOAuth2LoginAuthenticationEntryPoint(String loginSuccessMessage) {
        this.loginSuccessMessage = loginSuccessMessage;
    }

    public LightningOAuth2LoginAuthenticationEntryPoint() {

    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        // 直接返回 认证异常错误信息
        if(enableAuthErrorDetails != null && enableAuthErrorDetails) {
            AuthHttpResponseUtil.commence(
                    response,
                    JsonUtil.getDefaultJsonUtil().asJSON(
                            Result.error(
                                    ApplicationAuthException.auth2AuthenticationException().getCode(),
                                    exception.getMessage()
                            )
                    )
            );
        }
        else {

            if(StringUtils.hasText(authErrorMessage)) {
                AuthHttpResponseUtil.commence(
                        response,
                        JsonUtil.getDefaultJsonUtil().asJSON(
                                Result.error(
                                        ApplicationAuthException.accountNotFoundException().getCode(),
                                        authErrorMessage
                                )
                        )
                );
            }
            else {
                // 必然是 OAuth2AuthenticationException ...
                // 返回 401
                AuthHttpResponseUtil.commence(
                        response,
                        JsonUtil.getDefaultJsonUtil().asJSON(ApplicationAuthException.auth2AuthenticationException().asResult())
                );
            }
        }
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        ApplicationLevelAuthorizationToken oAuth2AuthenticationToken = (ApplicationLevelAuthorizationToken)authentication.getPrincipal();

        // 成功之后颁发 ApplicationLevelAuthorizationToken
        AuthHttpResponseUtil.commence(
                response,
                JsonUtil.getDefaultJsonUtil().asJSON(
                        Result.success(
                              200,
                                loginSuccessMessage
                              ,
                                oAuth2AuthenticationToken
                        )
                )
        );
    }


}
