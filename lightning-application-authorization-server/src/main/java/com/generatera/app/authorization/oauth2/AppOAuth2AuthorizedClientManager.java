package com.generatera.app.authorization.oauth2;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.util.Assert;

/**
 * @author FLJ
 * @date 2022/12/30
 * @time 16:30
 * @Description 尝试 使用它进行OAuth client 授权 ..
 */
public class AppOAuth2AuthorizedClientManager implements OAuth2AuthorizedClientManager {

    private OAuth2ClientProperties.Registration registration;

    public AppOAuth2AuthorizedClientManager(OAuth2ClientProperties oAuth2ClientProperties) {
        OAuth2ClientProperties.Registration registration = oAuth2ClientProperties.getRegistration().get("default");
        Assert.notNull(registration,"default registration must not be null !!!");
    }

    @Override
    public OAuth2AuthorizedClient authorize(OAuth2AuthorizeRequest authorizeRequest) {
        return null;
    }
}
