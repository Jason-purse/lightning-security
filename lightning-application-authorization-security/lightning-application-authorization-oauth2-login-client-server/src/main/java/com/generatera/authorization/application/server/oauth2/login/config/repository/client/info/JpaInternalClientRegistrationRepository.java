package com.generatera.authorization.application.server.oauth2.login.config.repository.client.info;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.client.info.ClientRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaInternalClientRegistrationRepository extends JpaRepository<ClientRegistrationEntity,String> {
}
