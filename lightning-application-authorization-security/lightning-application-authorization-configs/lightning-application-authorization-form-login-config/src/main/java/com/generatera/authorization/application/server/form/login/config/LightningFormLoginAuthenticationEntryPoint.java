package com.generatera.authorization.application.server.form.login.config;

import com.generatera.authorization.application.server.config.ApplicationAuthException;
import com.generatera.authorization.application.server.config.AuthHttpResponseUtil;
import com.jianyue.lightning.result.Result;
import com.jianyue.lightning.util.JsonUtil;
import lombok.Data;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * 进行表单登陆的 认证端点提示,当出现认证错误时 ..以及 登陆成功的处理器 ..
 * <p>
 * 1.成功 返回token
 * 2.失败 返回错误响应 401 并给出错误提示
 */
@Data
public class LightningFormLoginAuthenticationEntryPoint implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

    // ----- 如果需要后端 国际化消息响应  ----------
    private String loginSuccessMessage = "LOGIN_SUCCESS";

    private String loginFailureMessage = "";

    private String badCredentialsMessage = "";

    private String accountStatusExpiredMessage = "";

    private String accountStatusLockedMessage = "";

    /**
     * 开启状态状态详细通知
     */
    private Boolean enableAccountStatusInform = false;


    private String accountStatusMessage = "";





    @Override
    public void onAuthenticationFailure(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        Result<?> result = null;

        if(enableAccountStatusInform) {
            // 坏的凭证信息
            if (exception instanceof BadCredentialsException) {

                if(StringUtils.hasText(badCredentialsMessage)) {
                    result = Result.error(ApplicationAuthException.badCredentialsException().getCode(), badCredentialsMessage);
                }
                else {
                    result = ApplicationAuthException.badCredentialsException().asResult();
                }
            }

            // 状态异常
            else if(exception instanceof AccountStatusException) {

                if(exception instanceof AccountExpiredException) {

                    if(StringUtils.hasText(accountStatusExpiredMessage)) {
                        result = Result.error(
                                ApplicationAuthException.accountExpiredException().getCode(),
                                accountStatusExpiredMessage
                        );
                    }
                    else {
                        result = ApplicationAuthException.accountExpiredException().asResult();
                    }
                }
                else if(exception instanceof LockedException || exception instanceof DisabledException) {
                    if(StringUtils.hasText(accountStatusLockedMessage)) {
                        result =  Result.error(
                                ApplicationAuthException.accountLockedException().getCode(),
                                accountStatusLockedMessage
                        );
                    }
                    else {
                        result = ApplicationAuthException.accountLockedException().asResult();
                    }
                }

                // 无法区分的账户状态异常 ..
                if(result == null) {

                    if(StringUtils.hasText(accountStatusMessage)) {
                        result = Result.error(ApplicationAuthException.accountStatusException().getCode(),
                                accountStatusMessage);
                    }
                    else {
                        result = ApplicationAuthException.accountStatusException().asResult();
                    }
                }
            }
            // 其他状态异常 ..
            else {
                result = ApplicationAuthException.authOtherException().asResult();
            }
        }
        else {
            if(StringUtils.hasText(loginFailureMessage)) {
                result = Result.error(ApplicationAuthException.auth2AuthenticationException().getCode(),loginFailureMessage);
            }
            else {
                result = ApplicationAuthException.auth2AuthenticationException().asResult();
            }
        }


        AuthHttpResponseUtil.commence(response,
                JsonUtil.of().asJSON(result)
        );
    }

    @Override
    public void onAuthenticationSuccess(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 返回凭证信息
        Object principal = authentication.getPrincipal();
        AuthHttpResponseUtil.commence(
                response,
                JsonUtil.of().asJSON(
                        Result.success(200,loginSuccessMessage,principal)
                )
        );
    }
}
