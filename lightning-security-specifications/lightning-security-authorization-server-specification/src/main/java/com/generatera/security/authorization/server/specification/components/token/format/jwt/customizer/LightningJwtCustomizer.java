package com.generatera.security.authorization.server.specification.components.token.format.jwt.customizer;


public interface LightningJwtCustomizer {

	void customizeToken(JwtEncodingContext context);
	
}
