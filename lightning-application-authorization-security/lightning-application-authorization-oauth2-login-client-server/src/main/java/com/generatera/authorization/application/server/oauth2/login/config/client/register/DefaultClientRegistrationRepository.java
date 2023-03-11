package com.generatera.authorization.application.server.oauth2.login.config.client.register;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.client.info.ClientRegistrationEntity;

import java.util.Collections;
import java.util.List;

public class DefaultClientRegistrationRepository extends AbstractClientRegistrationRepository {
    @Override
    protected ClientRegistrationEntity internalFindByRegistrationId(String registrationId) {
        return null;
    }

    @Override
    protected List<ClientRegistrationEntity> internalFindAllRegistrations() {
        return Collections.emptyList();
    }
}
