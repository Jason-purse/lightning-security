package com.generatera.authorization.application.server.oauth2.login.config.repository.client.registration;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.registration.ClientRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaInternalClientRegistrationRepository extends JpaRepository<ClientRegistrationEntity,String> {
}
