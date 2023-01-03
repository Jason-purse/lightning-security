package com.generatera.authorization.server.oauth2.configuration.token.claims;

import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;
/**
 * @author FLJ
 * @date 2022/12/30
 * @time 15:38
 * @Description OAuth2Token Claims 自定义器 实现
 */
public class OAuth2TokenClaimsCustomizerImpl implements OAuth2TokenClaimsCustomizer {

	@Override
	public void customizeTokenClaims(OAuth2TokenClaimsContext context) {
		System.out.println("-----OAuth2TokenClaimsCustomizerImpl----" + context);
		
	}
	
}
