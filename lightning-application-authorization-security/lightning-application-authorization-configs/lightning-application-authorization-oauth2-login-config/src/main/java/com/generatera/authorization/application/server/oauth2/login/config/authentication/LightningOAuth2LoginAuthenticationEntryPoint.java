package com.generatera.authorization.application.server.oauth2.login.config.authentication;

import com.generatera.authorization.application.server.config.ApplicationAuthException;
import com.generatera.authorization.application.server.config.AuthHttpResponseUtil;
import com.generatera.authorization.application.server.config.specification.LightningAuthenticationTokenService;
import com.generatera.authorization.application.server.config.token.ApplicationLevelAuthorizationToken;
import com.generatera.authorization.application.server.oauth2.login.config.token.LightningOAuth2LoginAuthenticationTokenGenerator;
import com.generatera.security.application.authorization.server.token.specification.LightningApplicationLevelAuthenticationSecurityContext;
import com.generatera.security.application.authorization.server.token.specification.LightningApplicationLevelAuthenticationToken;
import com.generatera.security.authorization.server.specification.TokenSettingsProvider;
import com.generatera.security.authorization.server.specification.endpoints.provider.ProviderContextHolder;
import com.generatera.security.server.token.specification.LightningAuthorizationServerTokenSecurityContext;
import com.generatera.security.server.token.specification.LightningTokenType;
import com.generatera.security.server.token.specification.format.LightningTokenFormat;
import com.jianyue.lightning.result.Result;
import com.jianyue.lightning.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
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

    private LightningAuthenticationTokenService authenticationTokenService;




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

    @Autowired
    public void setTokenSettingsProvider(TokenSettingsProvider tokenSettingsProvider) {
        Assert.notNull(tokenSettingsProvider,"tokenSettingsProvider must not be null !!!");
        this.tokenSettingsProvider = tokenSettingsProvider;
    }

    public void setAuthenticationTokenService(LightningAuthenticationTokenService authenticationTokenService) {
        Assert.notNull(authenticationTokenService,"authenticationTokenService must not be null !!!");
        this.authenticationTokenService = authenticationTokenService;
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

        LightningApplicationLevelAuthenticationToken token = tokenGenerator.
                generate(
                        LightningApplicationLevelAuthenticationSecurityContext.of(
                                LightningAuthorizationServerTokenSecurityContext.of(
                                        LightningTokenType.LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE,
                                        LightningTokenFormat.JWT,
                                        LightningTokenType.LightningTokenValueType.BEARER_TOKEN_TYPE,
                                        authentication,
                                        com.generatera.security.authorization.server.specification.endpoints.provider.ProviderContextHolder.getProviderContext(),
                                        tokenSettingsProvider.getTokenSettings()
                                )
                        )
                );
        LightningApplicationLevelAuthenticationToken refreshToken = tokenGenerator.
                generate(
                        LightningApplicationLevelAuthenticationSecurityContext.of(
                                LightningAuthorizationServerTokenSecurityContext.of(
                                        LightningTokenType.LightningAuthenticationTokenType.REFRESH_TOKEN_TYPE,
                                        LightningTokenFormat.JWT,
                                        LightningTokenType.LightningTokenValueType.BEARER_TOKEN_TYPE,
                                        authentication,
                                        ProviderContextHolder.getProviderContext(),
                                        tokenSettingsProvider.getTokenSettings()
                                )
                        )
                );


        AuthHttpResponseUtil.commence(
                response,
                JsonUtil.of().asJSON(
                        Result.success(200, loginSuccessMessage,
                                ApplicationLevelAuthorizationToken.of(
                                        token,
                                        refreshToken)
                        )
                )
        );
    }


}
