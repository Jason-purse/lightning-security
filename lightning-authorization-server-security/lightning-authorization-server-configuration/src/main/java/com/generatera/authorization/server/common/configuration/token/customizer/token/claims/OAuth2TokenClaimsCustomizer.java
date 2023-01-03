package com.generatera.authorization.server.common.configuration.token.customizer.token.claims;

import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;

public interface OAuth2TokenClaimsCustomizer {

	void customizeTokenClaims(OAuth2TokenClaimsContext context);
	
}
