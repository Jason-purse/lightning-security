package com.generatera.authorization.application.server.form.login.config.authentication;

import com.generatera.authorization.application.server.config.ApplicationAuthException;
import com.generatera.authorization.application.server.config.token.ApplicationLevelAuthorizationToken;
import com.generatera.authorization.server.common.configuration.authorization.DefaultLightningAuthorization;
import com.generatera.authorization.server.common.configuration.authorization.store.LightningAuthenticationTokenService;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.TokenSettingsProvider;
import com.generatera.security.authorization.server.specification.components.provider.ProviderContextHolder;
import com.generatera.security.authorization.server.specification.components.token.*;
import com.generatera.security.authorization.server.specification.components.token.format.plain.UuidUtil;
import com.generatera.security.authorization.server.specification.util.AuthHttpResponseUtil;
import com.jianyue.lightning.result.Result;
import com.jianyue.lightning.util.JsonUtil;
import lombok.Data;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import java.io.IOException;

import static com.generatera.authorization.server.common.configuration.authorization.LightningAuthorization.USER_INFO_ATTRIBUTE_NAME;

/**
 * 进行表单登陆的 认证端点提示,当出现认证错误时 ..以及 登陆成功的处理器 ..
 * <p>
 * 1.成功 返回token
 * 2.失败 返回错误响应 401 并给出错误提示
 */
@Data
public class DefaultLightningFormLoginAuthenticationEntryPoint implements LightningFormLoginAuthenticationEntryPoint {

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


    private LightningTokenGenerator<LightningToken> tokenGenerator;

    private TokenSettingsProvider tokenSettingsProvider;

    private LightningAuthenticationTokenService authenticationTokenService;

    public DefaultLightningFormLoginAuthenticationEntryPoint(LightningTokenGenerator<LightningToken> tokenGenerator,
                                                             TokenSettingsProvider tokenSettingsProvider,
                                                             LightningAuthenticationTokenService authenticationTokenService) {
        Assert.notNull(
                tokenGenerator,
                "Form login tokenGenerator must not be null !!!"
        );
        Assert.notNull(tokenSettingsProvider,
                "token settings must not be null !!!");

        Assert.notNull(authenticationTokenService,
                "authentication token service must not be null !!!");
        this.tokenGenerator = tokenGenerator;
        this.tokenSettingsProvider = tokenSettingsProvider;
        this.authenticationTokenService = authenticationTokenService;
    }


    @Override
    public void onAuthenticationFailure(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        Result<?> result = null;

        if (enableAccountStatusInform) {
            // 坏的凭证信息
            if (exception instanceof BadCredentialsException) {

                if (StringUtils.hasText(badCredentialsMessage)) {
                    result = Result.error(ApplicationAuthException.badCredentialsException().getCode(), badCredentialsMessage);
                } else {
                    result = ApplicationAuthException.badCredentialsException().asResult();
                }
            }

            // 状态异常
            else if (exception instanceof AccountStatusException) {

                if (exception instanceof AccountExpiredException) {

                    if (StringUtils.hasText(accountStatusExpiredMessage)) {
                        result = Result.error(
                                ApplicationAuthException.accountExpiredException().getCode(),
                                accountStatusExpiredMessage
                        );
                    } else {
                        result = ApplicationAuthException.accountExpiredException().asResult();
                    }
                } else if (exception instanceof LockedException || exception instanceof DisabledException) {
                    if (StringUtils.hasText(accountStatusLockedMessage)) {
                        result = Result.error(
                                ApplicationAuthException.accountLockedException().getCode(),
                                accountStatusLockedMessage
                        );
                    } else {
                        result = ApplicationAuthException.accountLockedException().asResult();
                    }
                }

                // 无法区分的账户状态异常 ..
                if (result == null) {

                    if (StringUtils.hasText(accountStatusMessage)) {
                        result = Result.error(ApplicationAuthException.accountStatusException().getCode(),
                                accountStatusMessage);
                    } else {
                        result = ApplicationAuthException.accountStatusException().asResult();
                    }
                }
            }
            // 其他状态异常 ..
            else {
                result = ApplicationAuthException.authOtherException().asResult();
            }
        } else {
            if (StringUtils.hasText(loginFailureMessage)) {
                result = Result.error(ApplicationAuthException.auth2AuthenticationException().getCode(), loginFailureMessage);
            } else {
                result = ApplicationAuthException.auth2AuthenticationException().asResult();
            }
        }


        AuthHttpResponseUtil.commence(response,
                JsonUtil.of().asJSON(result)
        );
    }

    @Override
    public void onAuthenticationSuccess(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        LightningToken token = tokenGenerator.
                generate(
                        new LightningSecurityAccessTokenContext(LightningSecurityTokenContext.of(
                                authentication,
                                ProviderContextHolder.getProviderContext(),
                                tokenSettingsProvider.getTokenSettings(),
                                tokenSettingsProvider.getTokenSettings().getAccessTokenIssueFormat(),
                                tokenSettingsProvider.getTokenSettings().getAccessTokenValueType(),
                                LightningTokenType.LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE,
                                ((LightningUserPrincipal) authentication.getPrincipal())
                        ))
                );
        LightningToken refreshToken = tokenGenerator.
                generate(
                        new LightningSecurityAccessTokenContext(LightningSecurityTokenContext.of(
                                authentication,
                                ProviderContextHolder.getProviderContext(),
                                tokenSettingsProvider.getTokenSettings(),
                                tokenSettingsProvider.getTokenSettings().getAccessTokenIssueFormat(),
                                tokenSettingsProvider.getTokenSettings().getRefreshTokenValueType(),
                                LightningTokenType.LightningAuthenticationTokenType.REFRESH_TOKEN_TYPE,
                                ((LightningUserPrincipal) authentication.getPrincipal())
                        ))
                );


        // 凭证信息,也直接进行存储
        LightningUserPrincipal principal = ((LightningUserPrincipal) authentication.getPrincipal());

        DefaultLightningAuthorization authorization
                = new DefaultLightningAuthorization.Builder()
                .id(UuidUtil.nextId())
                .principalName(authentication.getName())
                .accessToken(((LightningToken.LightningAccessToken) token))
                .refreshToken(((LightningToken.LightningRefreshToken) refreshToken))
                .attribute(USER_INFO_ATTRIBUTE_NAME,JsonUtil.getDefaultJsonUtil().asJSON(principal))
                .build();

        AuthHttpResponseUtil.commence(
                response,
                JsonUtil.getDefaultJsonUtil().asJSON(
                        Result.success(200, loginSuccessMessage,
                                ApplicationLevelAuthorizationToken
                                        .of(token.getTokenValue(), refreshToken.getTokenValue())
                        )
                )
        );
    }
}
