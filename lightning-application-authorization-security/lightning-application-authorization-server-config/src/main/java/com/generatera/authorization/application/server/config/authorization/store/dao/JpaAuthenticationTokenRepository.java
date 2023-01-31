package com.generatera.authorization.application.server.config.authorization.store.dao;

import com.generatera.authorization.application.server.config.model.entity.ForDBAuthenticationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * jpa authentication token repository
 */
public interface JpaAuthenticationTokenRepository extends JpaRepository<ForDBAuthenticationTokenEntity,Long> {

    ForDBAuthenticationTokenEntity findFirstByAccessTokenValueIsOrRefreshTokenValueIs(String accessTokenValue, String refreshTokenValue);
}
