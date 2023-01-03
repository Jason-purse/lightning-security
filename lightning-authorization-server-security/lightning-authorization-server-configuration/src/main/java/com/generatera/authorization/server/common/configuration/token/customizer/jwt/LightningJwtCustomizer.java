package com.generatera.authorization.server.common.configuration.token.customizer.jwt;


import com.generatera.authorization.server.common.configuration.token.JwtEncodingContext;

public interface LightningJwtCustomizer {

	void customizeToken(JwtEncodingContext context);
	
}
