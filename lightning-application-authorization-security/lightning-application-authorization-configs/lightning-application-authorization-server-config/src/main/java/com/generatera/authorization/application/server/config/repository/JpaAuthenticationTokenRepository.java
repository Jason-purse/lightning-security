package com.generatera.authorization.application.server.config.repository;

import com.generatera.authorization.application.server.config.model.entity.LightningAuthenticationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAuthenticationTokenRepository extends JpaRepository<LightningAuthenticationTokenEntity,Long> {

    LightningAuthenticationTokenEntity findFirstByAccessTokenValueIsOrRefreshTokenValueIs(String accessTokenValue, String refreshTokenValue);
}
