package com.generatera.authorization.application.server.oauth2.login.config.client;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.ClientRegistrationEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

public abstract class AbstractClientRegistrationRepository implements LightningClientRegistrationRepository {

    private final Converter<ClientRegistrationEntity,ClientRegistration> clientRegistrationConverter = new ClientRegistrationConverter();
    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        ClientRegistrationEntity clientRegistrationEntity = internalFindByRegistrationId(registrationId);
        if(clientRegistrationEntity != null) {
            return clientRegistrationConverter.convert(clientRegistrationEntity);
        }
        return null;
    }

    protected abstract ClientRegistrationEntity internalFindByRegistrationId(String registrationId);
}
