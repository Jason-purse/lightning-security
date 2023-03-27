package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.server.common.configuration.LightningAuthorizationGrantType;
import com.generatera.security.authorization.server.specification.components.token.LightningToken.LightningAccessToken;
import com.generatera.security.authorization.server.specification.components.token.LightningToken.LightningRefreshToken;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;
/**
 * @author FLJ
 * @date 2023/1/28
 * @time 15:07
 * @Description 包含了访问 token / 刷新 token 信息 以及额外的参数信息 ..
 */
public class AuthAccessTokenAuthenticationToken extends AuthAuthorizationGrantAuthenticationToken {
    private final Authentication userPrincipal;

    @Nullable
    private final LightningAccessToken accessToken;

    @Nullable
    private final LightningRefreshToken refreshToken;

    public AuthAccessTokenAuthenticationToken(Authentication userPrincipal) {
        this(userPrincipal,null);
    }

    public AuthAccessTokenAuthenticationToken(Authentication userPrincipal, LightningAccessToken accessToken) {
        this(userPrincipal, accessToken, null);
    }

    public AuthAccessTokenAuthenticationToken(Authentication userPrincipal, @Nullable LightningAccessToken accessToken, @Nullable LightningRefreshToken refreshToken) {
        this(userPrincipal, accessToken, refreshToken, Collections.emptyMap());
    }

    public AuthAccessTokenAuthenticationToken(Authentication userPrincipal, @Nullable LightningAccessToken accessToken, @Nullable LightningRefreshToken refreshToken, Map<String, Object> additionalParameters) {
        super(LightningAuthorizationGrantType.ACCESS_TOKEN,userPrincipal,additionalParameters);
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userPrincipal = userPrincipal;
    }

    @Override
    public Object getPrincipal() {
        return this.userPrincipal.getPrincipal();
    }

    @Override
    public Object getCredentials() {
        return this.userPrincipal.getCredentials();
    }


    public LightningAccessToken getAccessToken() {
        return this.accessToken;
    }

    @Nullable
    public LightningRefreshToken getRefreshToken() {
        return this.refreshToken;
    }


    public Authentication getAuthentication() {
        return userPrincipal;
    }
}
