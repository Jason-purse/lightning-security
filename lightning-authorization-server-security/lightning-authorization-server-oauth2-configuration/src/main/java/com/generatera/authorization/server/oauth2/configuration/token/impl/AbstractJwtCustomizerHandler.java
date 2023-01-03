package com.generatera.authorization.server.oauth2.configuration.token.impl;

import com.generatera.authorization.server.common.configuration.token.LightningToken;
import com.generatera.authorization.server.common.configuration.token.customizer.jwt.JwtEncodingContext;
import com.generatera.authorization.server.common.configuration.token.customizer.jwt.LightningJwtCustomizerHandler;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;

public abstract class AbstractJwtCustomizerHandler implements LightningJwtCustomizerHandler {

	protected LightningJwtCustomizerHandler jwtCustomizerHandler;
	
	public AbstractJwtCustomizerHandler(LightningJwtCustomizerHandler jwtCustomizerHandler) {
		this.jwtCustomizerHandler = jwtCustomizerHandler;
	}

	protected abstract boolean supportCustomizeContext(Authentication authentication);
	protected abstract void customizeJwt(JwtEncodingContext jwtEncodingContext);
	
	@Override
	public void customize(JwtEncodingContext jwtEncodingContext) {
		
		boolean supportCustomizeContext = false;
		AbstractAuthenticationToken token = null;
    	
    	Authentication authenticataion = SecurityContextHolder.getContext().getAuthentication();
    	
    	if (authenticataion instanceof OAuth2ClientAuthenticationToken) {
    		token = (OAuth2ClientAuthenticationToken) authenticataion;
    	} 
    	
    	if (token != null) {
    		if (token.isAuthenticated() && LightningToken.TokenType.ACCESS_TOKEN_TYPE.equals(jwtEncodingContext.getTokenType())) {
    			Authentication authentication = jwtEncodingContext.getPrincipal();
    			supportCustomizeContext = supportCustomizeContext(authentication);
    		}
    	}
		
    	if (supportCustomizeContext) {
    		customizeJwt(jwtEncodingContext);
    	} else {
    		jwtCustomizerHandler.customize(jwtEncodingContext);
    	}

	}

}
