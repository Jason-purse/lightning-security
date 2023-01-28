package com.generatera.authorization.application.server.config.token;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.generatera.security.authorization.server.specification.components.token.LightningToken.LightningAccessToken;
import com.generatera.security.authorization.server.specification.components.token.LightningToken.LightningRefreshToken;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * @author FLJ
 * @date 2022/12/30
 * @time 15:50
 * @Description 应用级别的 授权token 派发
 */
public interface ApplicationLevelAuthorizationToken extends Serializable {

    /**
     * 获取OAuth2 访问 token
      */

    LightningAccessToken accessToken();

    /**
     * 获取OAuth2 刷新Token
     */
    LightningRefreshToken refreshToken();


    public static ApplicationLevelAuthorizationToken of(LightningAccessToken oAuth2AccessToken,LightningRefreshToken oAuth2RefreshToken) {
        return new DefaultApplicationLevelAuthorizationToken(oAuth2AccessToken,oAuth2RefreshToken);
    }
}

@AllArgsConstructor
class DefaultApplicationLevelAuthorizationToken implements ApplicationLevelAuthorizationToken {

    private final LightningAccessToken auth2AccessToken;

    private final LightningRefreshToken auth2RefreshToken;

    @Override
    @JsonGetter
    public LightningAccessToken accessToken() {
        return auth2AccessToken;
    }

    @Override
    @JsonGetter
    public LightningRefreshToken refreshToken() {
        return auth2RefreshToken;
    }
}
