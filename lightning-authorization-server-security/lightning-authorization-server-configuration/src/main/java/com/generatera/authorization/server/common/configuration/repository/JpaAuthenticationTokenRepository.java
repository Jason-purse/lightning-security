package com.generatera.authorization.server.common.configuration.repository;

import com.generatera.authorization.server.common.configuration.model.entity.LightningAuthenticationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAuthenticationTokenRepository extends JpaRepository<LightningAuthenticationTokenEntity,Long> {

    LightningAuthenticationTokenEntity findFirstByAccessTokenValueIsOrRefreshTokenValueIs(String accessTokenValue, String refreshTokenValue);
}
