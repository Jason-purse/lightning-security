package com.generatera.authorization.application.server.oauth2.login.config.authorization.request;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.AuthorizationRequestEntity;
import com.generatera.authorization.application.server.oauth2.login.config.repository.authorization.request.JpaInternalAuthorizationRequestRepository;
import org.springframework.data.domain.Example;
import org.springframework.util.Assert;

public class JpaAuthorizationRequestRepository extends AbstractLightningAuthorizationRequestRepository {

    private final JpaInternalAuthorizationRequestRepository repository;

    public JpaAuthorizationRequestRepository(JpaInternalAuthorizationRequestRepository repository) {
        Assert.notNull(repository,"repository must not be null !!!");
        this.repository = repository;
    }


    @Override
    protected AuthorizationRequestEntity getInternalAuthorizationRequestEntity(String stateParameter) {

        return repository.findOne(Example.of(AuthorizationRequestEntity.builder()
                .state(stateParameter)
                .build()))
                .orElse(null);
    }

    @Override
    protected void saveAuthorizationRequestEntity(AuthorizationRequestEntity entity) {
        repository.save(entity);
    }

    @Override
    protected AuthorizationRequestEntity removeAuthorizationRequestEntity(String stateParameter) {
        AuthorizationRequestEntity internalAuthorizationRequestEntity = getInternalAuthorizationRequestEntity(stateParameter);
        if(internalAuthorizationRequestEntity != null) {
            repository.deleteById(internalAuthorizationRequestEntity.getId());
            return internalAuthorizationRequestEntity;
        }

        return null;
    }
}
