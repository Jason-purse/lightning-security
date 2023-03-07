package com.generatera.authorization.application.server.config.authentication;

import com.generatera.authorization.application.server.config.ApplicationAuthException;
import com.generatera.authorization.application.server.config.token.ApplicationLevelAuthorizationToken;
import com.generatera.authorization.application.server.config.token.AuthAccessTokenAuthenticationToken;
import com.generatera.authorization.application.server.config.token.DefaultAuthAppLevelAuthorizationTokenResponseMapConverter;
import com.generatera.security.authorization.server.specification.util.LogUtil;
import com.generatera.security.authorization.server.specification.components.authentication.LightningAuthenticationEntryPoint;
import com.generatera.security.authorization.server.specification.util.AuthHttpResponseUtil;
import com.jianyue.lightning.result.Result;
import com.jianyue.lightning.util.JsonUtil;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 抽象认证 entry point
 */
public interface LightningAbstractAuthenticationEntryPoint extends LightningAuthenticationEntryPoint {

    DefaultAuthAppLevelAuthorizationTokenResponseMapConverter DEFAULT_MAP_AUTH_ACCESS_TOKEN_RESPONSE_CONVERTER = new DefaultAuthAppLevelAuthorizationTokenResponseMapConverter();

    @Nullable
    default String getLoginSuccessMessage() {
        return null;
    }

    @Nullable
    default String getBadCredentialsMessage() {
        return null;
    }

    @Nullable
    default String getAccountExceptionMessage() {
        return null;
    }

    @Nullable
    default String getAccountExpiredMessage() {
        return null;
    }

    @Nullable
    default String getAccountLockedMessage() {
        return null;
    }

    @Nullable
    default String getLoginFailureMessage() {
        return null;
    }

    default String getUnAuthenticatedMessage() {
        return null;
    }

    boolean enableAuthDetails();

    boolean enableAccountStatusDetails();

    default DefaultAuthAppLevelAuthorizationTokenResponseMapConverter getAuthAccessTokenResponseMapConverter() {
        return DEFAULT_MAP_AUTH_ACCESS_TOKEN_RESPONSE_CONVERTER;
    }

    @Override
    default void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        LogUtil.prettyLog("Authentication Failure Error !!!",exception);

        if (enableAuthDetails()) {
            if (exception instanceof AccountStatusException) {
                accountExceptionRoute(response, exception);
            } else if (exception instanceof BadCredentialsException || exception instanceof UsernameNotFoundException) {
                badCredentialsHandle(response);
            } else if (exception instanceof InternalAuthenticationServiceException authenticationServiceException) {
                internalServiceExceptionHandle(request, response, authenticationServiceException);
            } else if (exception instanceof InsufficientAuthenticationException) {
                unLoginExceptionHandle(response);
            } else {
                authCommonfailureHandle(response);
            }
        } else {
            if (exception instanceof InternalAuthenticationServiceException authenticationServiceException) {
                internalServiceExceptionHandle(request, response, authenticationServiceException);
            }
            else if(exception instanceof InsufficientAuthenticationException) {
                unLoginExceptionHandle(response);
            }
            else {
                // 通用认证失败 ...
                authCommonfailureHandle(response);
            }
        }
    }

    private void unLoginExceptionHandle(HttpServletResponse response) {
        if (StringUtils.hasText(getUnAuthenticatedMessage())) {
            // 未登录 自定义提示信息 ...
            AuthHttpResponseUtil.commence(response, JsonUtil.getDefaultJsonUtil().asJSON(Result.error(
                    ApplicationAuthException.unloginException().getCode(),
                    getUnAuthenticatedMessage()
            )));
        } else {
            AuthHttpResponseUtil.commence(response, JsonUtil.getDefaultJsonUtil().asJSON(ApplicationAuthException.unloginException().asResult()));
        }
    }

    private void internalServiceExceptionHandle(HttpServletRequest request, HttpServletResponse response, InternalAuthenticationServiceException authenticationServiceException) throws IOException, ServletException {
        Throwable cause = authenticationServiceException.getCause();
        if (cause != null) {
            if (cause instanceof AuthenticationException causeAuthException) {
                onAuthenticationFailure(request, response, causeAuthException);
            } else {
                authCommonfailureHandle(response);
            }
        } else {
            AuthHttpResponseUtil.commence(response,
                    JsonUtil.getDefaultJsonUtil().asJSON(
                            ApplicationAuthException.authenticationServiceException().asResult()
                    ));
        }
    }

    private void authCommonfailureHandle(HttpServletResponse response) {
        if (StringUtils.hasText(getLoginFailureMessage())) {
            AuthHttpResponseUtil.commence(response,
                    JsonUtil.getDefaultJsonUtil().asJSON(
                            Result.error(
                                    ApplicationAuthException.authenticationFailureException().getCode(),
                                    getLoginFailureMessage()
                            )
                    )
            );
        } else {
            AuthHttpResponseUtil.commence(
                    response,
                    JsonUtil.getDefaultJsonUtil().asJSON(
                            ApplicationAuthException.authenticationFailureException().asResult()
                    )
            );
        }
    }

    private void badCredentialsHandle(HttpServletResponse response) {
        if (StringUtils.hasText(getBadCredentialsMessage())) {
            AuthHttpResponseUtil.commence(
                    response,
                    JsonUtil.getDefaultJsonUtil().asJSON(
                            Result.error(ApplicationAuthException.badCredentialsException().getCode(),
                                    getBadCredentialsMessage())
                    )
            );
        } else {
            authCommonfailureHandle(response);
        }
    }

    private void accountExceptionRoute(HttpServletResponse response, AuthenticationException exception) {
        if (enableAccountStatusDetails()) {
            if (exception instanceof LockedException || exception instanceof DisabledException) {
                if (StringUtils.hasText(getAccountLockedMessage())) {
                    AuthHttpResponseUtil.commence(
                            response,
                            JsonUtil.getDefaultJsonUtil().asJSON(Result.error(ApplicationAuthException.accountLockedException().getCode(),
                                    getAccountLockedMessage()))
                    );
                }
            } else if (exception instanceof AccountExpiredException) {
                if (StringUtils.hasText(getAccountExpiredMessage())) {
                    AuthHttpResponseUtil.commence(
                            response,
                            JsonUtil.getDefaultJsonUtil().asJSON(Result.error(ApplicationAuthException.accountLockedException().getCode(),
                                    getAccountExpiredMessage()))
                    );
                }
            } else {
                accountStatusExceptionHandle(response);
            }
        } else {
            accountStatusExceptionHandle(response);
        }
    }

    private void accountStatusExceptionHandle(HttpServletResponse response) {
        if (StringUtils.hasText(getAccountExceptionMessage())) {
            AuthHttpResponseUtil.commence(response,
                    JsonUtil.getDefaultJsonUtil().asJSON(
                            Result.error(ApplicationAuthException.accountStatusException().getCode(),
                                    getAccountExceptionMessage())
                    ));
        } else {
            authCommonfailureHandle(response);
        }
    }

    @Override
    default void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        AuthAccessTokenAuthenticationToken authenticationToken = (AuthAccessTokenAuthenticationToken) authentication;

        AuthHttpResponseUtil.commence(
                response,
                JsonUtil.getDefaultJsonUtil().asJSON(
                        Result.success(200,
                                StringUtils.hasText(getLoginSuccessMessage()) ? getLoginSuccessMessage() : "AUTH_SUCCESS",
                                getAuthAccessTokenResponseMapConverter().convert(
                                        ApplicationLevelAuthorizationToken.of(
                                                authenticationToken.getAccessToken(),
                                                authenticationToken.getRefreshToken()
                                        )
                                ))
                )
        );
    }
}
