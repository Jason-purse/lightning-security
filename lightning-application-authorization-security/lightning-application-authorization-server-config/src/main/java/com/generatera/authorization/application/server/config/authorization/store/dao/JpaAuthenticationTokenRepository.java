package com.generatera.authorization.application.server.config.authorization.store.dao;

import com.generatera.authorization.application.server.config.model.entity.ForDBAuthenticationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * jpa authentication token repository
 */
public interface JpaAuthenticationTokenRepository extends JpaRepository<ForDBAuthenticationTokenEntity,String> {

    ForDBAuthenticationTokenEntity findFirstByAccessTokenValueIsOrRefreshTokenValueIs(String accessTokenValue, String refreshTokenValue);


    List<ForDBAuthenticationTokenEntity> findAllByAccessExpiredAtGreaterThanEqual(Long expiredAt);
}
