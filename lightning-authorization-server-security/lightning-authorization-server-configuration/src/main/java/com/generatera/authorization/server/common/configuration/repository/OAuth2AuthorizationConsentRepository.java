package com.generatera.authorization.server.common.configuration.repository;

import com.generatera.authorization.server.common.configuration.model.entity.OAuth2AuthorizationConsentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuth2AuthorizationConsentRepository extends JpaRepository<OAuth2AuthorizationConsentEntity, OAuth2AuthorizationConsentEntity.AuthorizationConsentId> {

	Optional<OAuth2AuthorizationConsentEntity> findByRegisteredClientIdAndPrincipalName(String registeredClientId, String principalName);
	void deleteByRegisteredClientIdAndPrincipalName(String registeredClientId, String principalName);
	
}
