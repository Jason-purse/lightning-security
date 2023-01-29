package com.generatera.authorization.application.server.config.token;

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
public class AuthAccessTokenAuthenticationToken extends AbstractAuthenticationToken {
    private final Authentication userPrincipal;
    private final LightningAccessToken accessToken;
    private final LightningRefreshToken refreshToken;
    private final Map<String, Object> additionalParameters;

    public AuthAccessTokenAuthenticationToken(Authentication userPrincipal, LightningAccessToken accessToken) {
        this(userPrincipal, accessToken, null);
    }

    public AuthAccessTokenAuthenticationToken(Authentication userPrincipal, LightningAccessToken accessToken, @Nullable LightningRefreshToken refreshToken) {
        this(userPrincipal, accessToken, refreshToken, Collections.emptyMap());
    }

    public AuthAccessTokenAuthenticationToken(Authentication userPrincipal, LightningAccessToken accessToken, @Nullable LightningRefreshToken refreshToken, Map<String, Object> additionalParameters) {
        super(Collections.emptyList());
        Assert.notNull(userPrincipal, "userPrincipal cannot be null");
        Assert.notNull(accessToken, "accessToken cannot be null");
        Assert.notNull(additionalParameters, "additionalParameters cannot be null");
        this.userPrincipal = userPrincipal;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.additionalParameters = additionalParameters;
    }

    public Object getPrincipal() {
        return this.userPrincipal;
    }

    public Object getCredentials() {
        return "";
    }


    public LightningAccessToken getAccessToken() {
        return this.accessToken;
    }

    @Nullable
    public LightningRefreshToken getRefreshToken() {
        return this.refreshToken;
    }

    public Map<String, Object> getAdditionalParameters() {
        return this.additionalParameters;
    }

}
