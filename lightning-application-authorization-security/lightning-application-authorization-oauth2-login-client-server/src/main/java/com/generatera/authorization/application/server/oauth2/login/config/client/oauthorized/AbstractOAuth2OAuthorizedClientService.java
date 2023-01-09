package com.generatera.authorization.application.server.oauth2.login.config.client.oauthorized;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.OAuthorizedClientEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

public abstract class AbstractOAuth2OAuthorizedClientService implements LightningOAuthorizedClientService {

    private final OAuth2AuthorizedClientEntityConverter entityConverter = new OAuth2AuthorizedClientEntityConverter();
    private final OAuth2AuthorizedClientConverter clientConverter = new OAuth2AuthorizedClientConverter();

    @Override
    @SuppressWarnings("unchecked")
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, String principalName) {
        OAuthorizedClientEntity oAuthorizedClientEntity = internalLoadAuthorizedClient(clientRegistrationId, principalName);
        if (oAuthorizedClientEntity != null) {
            return (T) clientConverter.convert(oAuthorizedClientEntity);
        }
        return null;
    }

    protected abstract OAuthorizedClientEntity internalLoadAuthorizedClient(String clientRegistrationId, String principalName);

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
        internalSaveAuthorizedClient(entityConverter.convert(authorizedClient));
    }

    protected abstract void internalSaveAuthorizedClient(OAuthorizedClientEntity oAuthorizedClientEntity);

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
        internalRemoveAuthorizedClient(
                OAuthorizedClientEntity
                        .builder()
                        .clientRegistrationId(clientRegistrationId)
                        .principalName(principalName)
                        .build()
        );
    }

    protected abstract void internalRemoveAuthorizedClient(OAuthorizedClientEntity entity);
}
