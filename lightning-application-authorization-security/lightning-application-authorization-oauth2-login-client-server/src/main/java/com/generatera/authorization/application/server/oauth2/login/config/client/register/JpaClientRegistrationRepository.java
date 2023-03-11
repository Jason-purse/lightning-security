package com.generatera.authorization.application.server.oauth2.login.config.client.register;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.client.info.ClientRegistrationEntity;
import com.generatera.authorization.application.server.oauth2.login.config.repository.client.info.JpaInternalClientRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Example;

import java.util.List;


@RequiredArgsConstructor
public class JpaClientRegistrationRepository extends AbstractClientRegistrationRepository {

    private final JpaInternalClientRegistrationRepository repository;


    @Override
    @Nullable
    protected ClientRegistrationEntity internalFindByRegistrationId(String registrationId) {
        return repository
                .findOne(Example.of(
                        ClientRegistrationEntity.builder()
                                .registrationId(registrationId)
                                .build()
                ))
                .orElse(null);
    }

    @Override
    protected List<ClientRegistrationEntity> internalFindAllRegistrations() {
        return repository.findAll();
    }
}
