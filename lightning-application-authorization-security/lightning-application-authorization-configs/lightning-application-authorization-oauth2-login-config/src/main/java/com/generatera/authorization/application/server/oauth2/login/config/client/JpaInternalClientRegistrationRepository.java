package com.generatera.authorization.application.server.oauth2.login.config.client;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.ClientRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaInternalClientRegistrationRepository extends JpaRepository<ClientRegistrationEntity,String> {
}
