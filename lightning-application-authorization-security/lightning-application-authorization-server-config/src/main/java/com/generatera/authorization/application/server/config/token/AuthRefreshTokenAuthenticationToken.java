package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.application.server.config.LoginGrantType;
import com.generatera.authorization.server.common.configuration.AuthorizationGrantType;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * 注意,认证token的 principal 可能为空,当未认证的情况下 ..
 *
 * 认证之后,principal 不为空 ..
 */
public class AuthRefreshTokenAuthenticationToken extends AuthAuthorizationGrantAuthenticationToken {
    private final String refreshToken;

    private final LoginGrantType loginGrantType;

    public AuthRefreshTokenAuthenticationToken(LoginGrantType loginGrantType,String refreshToken,@Nullable Map<String, Object> additionalParameters) {
        this(loginGrantType,refreshToken,null,additionalParameters);
    }

    public AuthRefreshTokenAuthenticationToken(LoginGrantType loginGrantType,String refreshToken, Authentication authentication, @Nullable Map<String, Object> additionalParameters) {
        super(AuthorizationGrantType.REFRESH_TOKEN,authentication,additionalParameters);
        Assert.notNull(loginGrantType,"login grant type must not be null !!!");
        Assert.hasText(refreshToken, "refreshToken cannot be empty");
        this.refreshToken = refreshToken;
        this.loginGrantType = loginGrantType;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public LoginGrantType getLoginGrantType() {
        return loginGrantType;
    }
}
