package com.generatera.authorization.server.common.configuration.token.customizer.jwt;


public interface LightningJwtCustomizerHandler {

	void customize(JwtEncodingContext jwtEncodingContext);

}
