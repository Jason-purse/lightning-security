package com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store.repository;

import com.generatera.central.oauth2.authorization.server.configuration.model.entity.authorization.JpaOAuth2AuthorizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OAuth2AuthorizationRepository extends JpaRepository<JpaOAuth2AuthorizationEntity, String> {

	Optional<JpaOAuth2AuthorizationEntity> findByState(String state);
	Optional<JpaOAuth2AuthorizationEntity> findByAuthorizationCodeValue(String authorizationCode);
	Optional<JpaOAuth2AuthorizationEntity> findByAccessTokenValue(String accessToken);
	Optional<JpaOAuth2AuthorizationEntity> findByRefreshTokenValue(String refreshToken);
	
	@Query("select a from JpaOAuth2AuthorizationEntity a where a.state = :token" +
			" or a.authorizationCodeValue = :token" +
			" or a.accessTokenValue = :token" +
			" or a.refreshTokenValue = :token"
	)
	Optional<JpaOAuth2AuthorizationEntity> findByStateOrAuthorizationCodeValueOrAccessTokenValueOrRefreshTokenValue(@Param("token") String token);
	
}