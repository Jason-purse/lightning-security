package com.generatera.authorization.server.common.configuration.token.customizer.jwt.impl;

import com.generatera.authorization.server.common.configuration.token.JwtEncodingContext;
import com.generatera.authorization.server.common.configuration.token.customizer.jwt.LightningJwtCustomizerHandler;

public class DefaultJwtCustomizerHandler implements LightningJwtCustomizerHandler {

	@Override
	public void customize(JwtEncodingContext jwtEncodingContext) {
		// does not modify any thing in context

	}

}
