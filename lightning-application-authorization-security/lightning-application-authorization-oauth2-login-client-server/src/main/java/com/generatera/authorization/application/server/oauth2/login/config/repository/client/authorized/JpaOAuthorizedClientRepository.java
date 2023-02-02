package com.generatera.authorization.application.server.oauth2.login.config.repository.client.authorized;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.OAuthorizedClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOAuthorizedClientRepository extends JpaRepository<OAuthorizedClientEntity,String> {
}
