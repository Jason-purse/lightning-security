package com.generatera.authorization.oauth2.repository;

import com.generatera.authorization.oauth2.entity.OAuth2RegisteredClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuth2RegisteredClientRepository extends JpaRepository<OAuth2RegisteredClient, String> {
	Optional<OAuth2RegisteredClient> findByClientId(String clientId);
}
