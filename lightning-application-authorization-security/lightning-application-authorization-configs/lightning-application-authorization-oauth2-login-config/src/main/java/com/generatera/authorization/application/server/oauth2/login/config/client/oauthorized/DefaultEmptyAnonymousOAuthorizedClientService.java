package com.generatera.authorization.application.server.oauth2.login.config.client.oauthorized;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

/**
 * 不存储任何OAuthorizedClient
 */
public class DefaultEmptyAnonymousOAuthorizedClientService implements LightningAnonymousOAuthorizedClientRepository {
    @Override
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, String principalName) {
        return null;
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
        // pass
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, String principalName) {

        // pass
    }
}
