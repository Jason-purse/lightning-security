package com.generatera.app.authorization.oauth2;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

/**
 * @author FLJ
 * @date 2022/12/30
 * @time 16:05
 * @Description 客户端注册仓库(里面只提供自己的客户端配置)
 */
@Repository
@AllArgsConstructor
public class ApplicationClientRegisteredRepository implements ClientRegistrationRepository {

    private OAuth2ClientProperties.Registration registeredClient;

    public ApplicationClientRegisteredRepository(OAuth2ClientProperties oAuth2ClientProperties) {
        OAuth2ClientProperties.Registration registration = oAuth2ClientProperties.getRegistration().get("default");
        Assert.notNull(registration,"spring.security.oauth2.client.registration.default must not be null !!!");
        this.registeredClient = registration;
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return ClientRegistration.withClientRegistration()
    }
}
