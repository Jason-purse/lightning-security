package com.generatera.authorization.application.server.oauth2.login.config.client;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.ClientRegistrationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;

/**
 * todo 可空性判断
 */
@RequiredArgsConstructor
public class JpaClientRegistrationRepository extends AbstractClientRegistrationRepository {

    private final JpaInternalClientRegistrationRepository repository;


    @Override
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
