package com.generatera.authorization.application.server.oauth2.login.config.client.oauthorized;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.OAuthorizedClientEntity;
import org.springframework.data.domain.Example;
import org.springframework.util.Assert;

public class JpaOAuthorizedClientService extends AbstractOAuth2OAuthorizedClientService {

    public final JpaOAuthorizedClientRepository repository;

    public JpaOAuthorizedClientService(JpaOAuthorizedClientRepository repository) {
        Assert.notNull(repository, "Jpa OAuthorized client repository must not be null !!!!");
        this.repository = repository;
    }


    @Override
    protected OAuthorizedClientEntity internalLoadAuthorizedClient(String clientRegistrationId, String principalName) {
        return repository.findOne(
                        Example.of(
                                OAuthorizedClientEntity.builder()
                                        .clientRegistrationId(clientRegistrationId)
                                        .principalName(principalName)
                                        .build()

                        )
                )
                .orElse(null);
    }


    @Override
    protected void internalSaveAuthorizedClient(OAuthorizedClientEntity oAuthorizedClientEntity) {
        repository.save(
                oAuthorizedClientEntity
        );
    }



    @Override
    protected void internalRemoveAuthorizedClient(OAuthorizedClientEntity entity) {
        repository.delete(entity);
    }
}
