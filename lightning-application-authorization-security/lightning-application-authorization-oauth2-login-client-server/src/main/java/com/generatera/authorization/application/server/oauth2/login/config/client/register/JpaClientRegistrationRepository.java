package com.generatera.authorization.application.server.oauth2.login.config.client.register;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.ClientRegistrationEntity;
import com.generatera.authorization.application.server.oauth2.login.config.repository.client.JpaInternalClientRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Example;


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
}
