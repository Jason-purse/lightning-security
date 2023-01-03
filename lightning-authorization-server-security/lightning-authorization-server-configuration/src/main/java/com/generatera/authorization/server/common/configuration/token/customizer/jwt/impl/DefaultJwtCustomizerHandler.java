package com.generatera.authorization.server.common.configuration.token.customizer.jwt.impl;

import com.generatera.authorization.server.common.configuration.token.customizer.jwt.LightningJwtCustomizerHandler;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

public class DefaultJwtCustomizerHandler implements LightningJwtCustomizerHandler {

	@Override
	public void customize(JwtEncodingContext jwtEncodingContext) {
		// does not modify any thing in context

	}

}
