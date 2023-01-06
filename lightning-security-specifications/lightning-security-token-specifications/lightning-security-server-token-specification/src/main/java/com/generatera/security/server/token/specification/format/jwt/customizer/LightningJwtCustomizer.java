package com.generatera.security.server.token.specification.format.jwt.customizer;


public interface LightningJwtCustomizer {

	void customizeToken(JwtEncodingContext context);
	
}
