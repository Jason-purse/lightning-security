package com.generatera.authorization.application.server.oauth2.login.config.authorization.request;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.AuthorizationRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaInternalAuthorizationRequestRepository extends JpaRepository<AuthorizationRequestEntity,String> {
}
