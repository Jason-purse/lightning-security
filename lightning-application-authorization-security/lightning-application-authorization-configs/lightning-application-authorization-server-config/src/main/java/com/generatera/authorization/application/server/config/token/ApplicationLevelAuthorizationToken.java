package com.generatera.authorization.application.server.config.token;

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
    Object getOAuth2AccessToken();

    /**
     * 获取OAuth2 刷新Token
     */
    Object getOAuth2RefreshToken();


    public static ApplicationLevelAuthorizationToken of(Object oAuth2AccessToken,Object oAuth2RefreshToken) {
        return new DefaultApplicationLevelAuthorizationToken(oAuth2AccessToken,oAuth2RefreshToken);
    }
}

@AllArgsConstructor
class DefaultApplicationLevelAuthorizationToken implements ApplicationLevelAuthorizationToken {

    private final Object auth2AccessToken;

    private final Object auth2RefreshToken;

    @Override
    public Object getOAuth2AccessToken() {
        return auth2AccessToken;
    }

    @Override
    public Object getOAuth2RefreshToken() {
        return auth2RefreshToken;
    }
}
