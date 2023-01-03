package com.generatera.authorization.server.common.configuration.token.customizer.jwt.impl;


import com.generatera.authorization.server.common.configuration.token.customizer.jwt.LightningJwtCustomizer;
import com.generatera.authorization.server.common.configuration.token.customizer.jwt.LightningJwtCustomizerHandler;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

public class JwtCustomizerImpl implements LightningJwtCustomizer {

	private final LightningJwtCustomizerHandler jwtCustomizerHandler;
	
	public JwtCustomizerImpl(LightningJwtCustomizerHandler jwtCustomizerHandler) {
		this.jwtCustomizerHandler = jwtCustomizerHandler;
	}

	@Override
	public void customizeToken(JwtEncodingContext jwtEncodingContext) {
		jwtCustomizerHandler.customize(jwtEncodingContext);
	}
	
}
