package com.generatera.authorization.server.common.configuration.token.customizer.jwt;


public interface LightningJwtCustomizer {

	void customizeToken(JwtEncodingContext context);
	
}
