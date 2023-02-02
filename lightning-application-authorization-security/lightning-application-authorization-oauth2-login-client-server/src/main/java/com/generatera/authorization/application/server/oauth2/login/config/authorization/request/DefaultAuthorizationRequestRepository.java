package com.generatera.authorization.application.server.oauth2.login.config.authorization.request;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.AuthorizationRequestEntity;

import java.util.concurrent.ConcurrentHashMap;

public class DefaultAuthorizationRequestRepository extends AbstractLightningAuthorizationRequestRepository {

    private final ConcurrentHashMap<String,AuthorizationRequestEntity> cache = new ConcurrentHashMap<>();

    @Override
    protected AuthorizationRequestEntity getInternalAuthorizationRequestEntity(String stateParameter) {
        return cache.get(stateParameter);
    }

    @Override
    protected void saveAuthorizationRequestEntity(AuthorizationRequestEntity entity) {
        cache.put(entity.getState(),entity);
    }

    @Override
    protected AuthorizationRequestEntity removeAuthorizationRequestEntity(String stateParameter) {
        return cache.remove(stateParameter);
    }
}
