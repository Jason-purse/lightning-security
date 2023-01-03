package com.generatera.authorization.server.common.configuration.token.customizer.jwt;


import com.generatera.authorization.server.common.configuration.token.JwtEncodingContext;

public interface LightningJwtCustomizerHandler {

	void customize(JwtEncodingContext jwtEncodingContext);



}
