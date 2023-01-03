package com.generatera.authorization.application.server.oauth2.login.config.authentication;

import com.generatera.authorization.application.server.config.ApplicationAuthException;
import com.generatera.authorization.application.server.config.AuthHttpResponseUtil;
import com.generatera.authorization.application.server.oauth2.login.config.token.LightningOAuth2LoginAuthenticationTokenGenerator;
import com.generatera.authorization.server.common.configuration.token.*;
import com.jianyue.lightning.result.Result;
import com.jianyue.lightning.util.JsonUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LightningOAuth2LoginAuthenticationEntryPoint implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

    private String loginSuccessMessage = "LOGIN_SUCCESS";

    /**
     * 一般开发阶段才需要此标识
     */
    private Boolean enableAuthErrorDetails = false;

    private String authErrorMessage = "LOGIN_FAILURE";


    private LightningOAuth2LoginAuthenticationTokenGenerator tokenGenerator;

    private TokenSettingsProvider tokenSettingsProvider;


    public void setTokenGenerator(LightningOAuth2LoginAuthenticationTokenGenerator tokenGenerator) {
        Assert.notNull(tokenGenerator, "tokenGenerator must not be null !!!");
        this.tokenGenerator = tokenGenerator;
    }

    public void setLoginSuccessMessage(String loginSuccessMessage) {
        Assert.hasText(loginSuccessMessage, "loginSuccessMessage must not be blank !!!");
        this.loginSuccessMessage = loginSuccessMessage;
    }

    public void setEnableAuthErrorDetails(Boolean enableAuthErrorDetails) {
        Assert.notNull(enableAuthErrorDetails, "enableAuthErrorDetails must not be null !!!");
        this.enableAuthErrorDetails = enableAuthErrorDetails;
    }

    public void setAuthErrorMessage(String authErrorMessage) {
        Assert.hasText(authErrorMessage, "authErrorMessage must not be blank !!!");
        this.authErrorMessage = authErrorMessage;
    }

    public void setTokenSettingsProvider(TokenSettingsProvider tokenSettingsProvider) {
        Assert.notNull(tokenSettingsProvider,"tokenSettingsProvider must not be null !!!");
        this.tokenSettingsProvider = tokenSettingsProvider;
    }

    public LightningOAuth2LoginAuthenticationEntryPoint() {

    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        // 直接返回 认证异常错误信息
        if (enableAuthErrorDetails != null && enableAuthErrorDetails) {
            AuthHttpResponseUtil.commence(
                    response,
                    JsonUtil.getDefaultJsonUtil().asJSON(
                            Result.error(
                                    ApplicationAuthException.auth2AuthenticationException().getCode(),
                                    exception.getMessage()
                            )
                    )
            );
        } else {

            if (StringUtils.hasText(authErrorMessage)) {
                AuthHttpResponseUtil.commence(
                        response,
                        JsonUtil.getDefaultJsonUtil().asJSON(
                                Result.error(
                                        ApplicationAuthException.accountNotFoundException().getCode(),
                                        authErrorMessage
                                )
                        )
                );
            } else {
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
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        // 生成 Token ..
        LightningAuthenticationToken token = tokenGenerator.generate(
                LightningAuthenticationSecurityContext.of(
                        authentication,
                        ProviderContextHolder.getProviderContext(),
                        tokenSettingsProvider.getTokenSettings()
                )
        );

        AuthHttpResponseUtil.commence(
                response,
                JsonUtil.getDefaultJsonUtil().asJSON(
                        Result.success(
                                200,
                                loginSuccessMessage
                                ,
                                token
                        )
                )
        );
    }


}
