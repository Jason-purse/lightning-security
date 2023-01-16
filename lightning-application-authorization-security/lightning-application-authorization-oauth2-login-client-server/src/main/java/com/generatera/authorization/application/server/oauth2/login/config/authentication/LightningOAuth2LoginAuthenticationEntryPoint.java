package com.generatera.authorization.application.server.oauth2.login.config.authentication;

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

import static com.generatera.authorization.server.common.configuration.authorization.LightningAuthorization.USER_INFO_ATTRIBUTE_NAME;
/**
 * @author FLJ
 * @date 2023/1/16
 * @time 14:03
 * @Description  可以进行简单的提示信息 注入 ...
 *
 * 其次可以配置token生成器进行 token 生成(包括访问 token / 刷新 token)
 *
 * 另外,颁发的token会通过 LightningAuthenticationTokenService 进行保存,以便 opaque token 能够进行 token Introspect端点进行检测 ..
 */
public class LightningOAuth2LoginAuthenticationEntryPoint implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

    private String loginSuccessMessage = "LOGIN_SUCCESS";

    /**
     * 一般开发阶段才需要此标识
     */
    private Boolean enableAuthErrorDetails = false;

    private String authErrorMessage = "LOGIN_FAILURE";


    private LightningTokenGenerator<LightningToken> tokenGenerator;

    private TokenSettingsProvider tokenSettingsProvider;

    private LightningAuthenticationTokenService authenticationTokenService;


    public void setTokenGenerator(LightningTokenGenerator<LightningToken> tokenGenerator) {
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
        Assert.notNull(tokenSettingsProvider, "tokenSettingsProvider must not be null !!!");
        this.tokenSettingsProvider = tokenSettingsProvider;
    }

    public void setAuthenticationTokenService(LightningAuthenticationTokenService authenticationTokenService) {
        Assert.notNull(authenticationTokenService, "authenticationTokenService must not be null !!!");
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

        // todo 上下文所需内容还需要 仔细审查

        LightningToken token = tokenGenerator.
                generate(
                        new LightningAccessTokenContext(
                                DefaultLightningTokenContext.builder()
                                        .authentication(authentication)
                                        .providerContext(ProviderContextHolder.getProviderContext())
                                        .tokenSettings(tokenSettingsProvider.getTokenSettings())
                                        .tokenValueType(tokenSettingsProvider.getTokenSettings().getAccessTokenValueType())
                                        .tokenIssueFormat(tokenSettingsProvider.getTokenSettings().getAccessTokenIssueFormat())
                                        .tokenValueFormat(tokenSettingsProvider.getTokenSettings().getAccessTokenValueFormat())
                                        .tokenType(LightningTokenType.LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE)
                                        .build()
                        )
                );
        LightningToken refreshToken = tokenGenerator.
                generate(
                        new LightningAccessTokenContext(
                                DefaultLightningTokenContext.builder()
                                        .authentication(authentication)
                                        .providerContext(ProviderContextHolder.getProviderContext())
                                        .tokenSettings(tokenSettingsProvider.getTokenSettings())
                                        .tokenValueType(tokenSettingsProvider.getTokenSettings().getRefreshTokenValueType())
                                        .tokenIssueFormat(tokenSettingsProvider.getTokenSettings().getRefreshTokenIssueFormat())
                                         // todo
                                        .tokenValueFormat(tokenSettingsProvider.getTokenSettings().getAccessTokenValueFormat())
                                        .tokenType(LightningTokenType.LightningAuthenticationTokenType.REFRESH_TOKEN_TYPE)
                                        .build()
                                )
                );


        // 凭证信息,也直接进行存储
        LightningUserPrincipal principal = ((LightningUserPrincipal) authentication.getPrincipal());

        // lightningJwt 需要进行转换(因为jwksource 的性质决定它可能根据不同的token生成器进行 token生成)...
        // todo 考虑token 是否可以永久有效 ..
        LightningToken.ComplexToken complexToken = (LightningToken.ComplexToken) token;
        LightningAccessTokenGenerator.LightningAuthenticationAccessToken accessToken
                = new LightningAccessTokenGenerator.LightningAuthenticationAccessToken(
                token, complexToken.getTokenValueType(),
                tokenSettingsProvider
                        .getTokenSettings()
                        .getAccessTokenValueFormat()
        );


        DefaultLightningAuthorization authorization
                = new DefaultLightningAuthorization.Builder()
                .id(UuidUtil.nextId())
                .principalName(authentication.getName())
                .accessToken(accessToken)
                .refreshToken(((LightningToken.LightningRefreshToken) refreshToken))
                // 用户信息 ..
                .attribute(USER_INFO_ATTRIBUTE_NAME, principal)
                .build();


        authenticationTokenService.save(authorization);

        AuthHttpResponseUtil.commence(
                response,
                JsonUtil.of().asJSON(
                        Result.success(200, loginSuccessMessage,
                                ApplicationLevelAuthorizationToken.of(
                                        token.getTokenValue(),
                                        refreshToken.getTokenValue())
                        )
                )
        );
    }


}
