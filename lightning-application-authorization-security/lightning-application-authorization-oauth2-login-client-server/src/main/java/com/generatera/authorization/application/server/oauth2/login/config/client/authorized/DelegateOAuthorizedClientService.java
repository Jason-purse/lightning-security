package com.generatera.authorization.application.server.oauth2.login.config.client.authorized;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.util.Assert;

/**
 * delegate ..
 */
public class DelegateOAuthorizedClientService implements LightningOAuthorizedClientService {
    private final OAuth2AuthorizedClientService clientService;
    public DelegateOAuthorizedClientService(OAuth2AuthorizedClientService clientService) {
        Assert.notNull(clientService,"OAuth2 AuthorizedClientService must not be null");
        this.clientService = clientService;
    }
    @Override
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, String principalName) {
        return clientService.loadAuthorizedClient(clientRegistrationId,principalName);
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
        this.clientService.saveAuthorizedClient(authorizedClient,principal);
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
        this.clientService.removeAuthorizedClient(clientRegistrationId,principalName);
    }
}
