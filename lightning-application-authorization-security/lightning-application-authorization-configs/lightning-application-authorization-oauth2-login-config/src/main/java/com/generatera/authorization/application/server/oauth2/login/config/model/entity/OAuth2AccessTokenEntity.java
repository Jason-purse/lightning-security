package com.generatera.authorization.application.server.oauth2.login.config.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.io.Serializable;
import java.time.Instant;

/**
 * 不单独存储
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OAuth2AccessTokenEntity implements Serializable {

    @Column(name = "access_token_value", length = 4000)
    private String accessTokenValue;

    @Column(name = "access_token_issued_at")
    private Instant accessTokenIssuedAt;

    @Column(name = "access_token_expires_at")
    private Instant accessTokenExpiresAt;

    @Column(name = "access_token_metadata", length = 2000)
    private String accessTokenMetadata;

    @Column(name = "access_token_type")
    private String accessTokenType;

    @Column(name = "access_token_scopes", length = 1000)
    private String accessTokenScopes;
}
