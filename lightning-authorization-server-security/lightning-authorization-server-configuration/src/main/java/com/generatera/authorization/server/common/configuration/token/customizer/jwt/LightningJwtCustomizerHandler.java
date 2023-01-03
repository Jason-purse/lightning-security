package com.generatera.authorization.server.common.configuration.token.customizer.jwt;

import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

public interface LightningJwtCustomizerHandler {

	void customize(JwtEncodingContext jwtEncodingContext);



}
