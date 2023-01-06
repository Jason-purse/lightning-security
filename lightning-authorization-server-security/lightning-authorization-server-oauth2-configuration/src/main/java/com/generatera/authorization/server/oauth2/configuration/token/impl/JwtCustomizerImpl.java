//package com.generatera.authorization.server.oauth2.configuration.token.impl;
//
//
//import com.generatera.authorization.server.common.configuration.token.customizer.jwt.JwtEncodingContext;
//import com.generatera.authorization.server.common.configuration.token.customizer.jwt.LightningJwtCustomizer;
//import com.generatera.authorization.server.common.configuration.token.customizer.jwt.LightningJwtCustomizerHandler;
//
//public class JwtCustomizerImpl implements LightningJwtCustomizer {
//
//	private final LightningJwtCustomizerHandler jwtCustomizerHandler;
//
//	public JwtCustomizerImpl(LightningJwtCustomizerHandler jwtCustomizerHandler) {
//		this.jwtCustomizerHandler = jwtCustomizerHandler;
//	}
//
//	@Override
//	public void customizeToken(JwtEncodingContext jwtEncodingContext) {
//		jwtCustomizerHandler.customize(jwtEncodingContext);
//	}
//
//}
