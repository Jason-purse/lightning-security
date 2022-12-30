package com.generatera.app.authorization.model.vo;

import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;

/**
 * @author FLJ
 * @date 2022/12/30
 * @time 15:50
 * @Description 应用级别的 授权token 派发
 */
public interface ApplicationLevelAuthorizationToken {

    /**
     * 获取OAuth2 访问 token
      */
    OAuth2AccessToken getOAuth2AccessToken();

    /**
     * 获取OAuth2 刷新Token
     */
    OAuth2RefreshToken getOAuth2RefreshToken();

}
