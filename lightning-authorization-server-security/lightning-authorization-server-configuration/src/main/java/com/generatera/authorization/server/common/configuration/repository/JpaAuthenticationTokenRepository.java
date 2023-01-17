package com.generatera.authorization.server.common.configuration.repository;

import com.generatera.authorization.server.common.configuration.model.entity.ForDBAuthenticationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAuthenticationTokenRepository extends JpaRepository<ForDBAuthenticationTokenEntity,Long> {

    ForDBAuthenticationTokenEntity findFirstByAccessTokenValueIsOrRefreshTokenValueIs(String accessTokenValue, String refreshTokenValue);
}
