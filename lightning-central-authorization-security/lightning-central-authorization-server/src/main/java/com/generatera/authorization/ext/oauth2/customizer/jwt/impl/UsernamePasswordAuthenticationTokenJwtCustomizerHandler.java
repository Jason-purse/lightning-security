package com.generatera.authorization.ext.oauth2.customizer.jwt.impl;

import com.generatera.authorization.ext.oauth2.customizer.jwt.JwtCustomizerHandler;
import com.generatera.authorization.oauth2.entity.LightningOAuth2UserDetails;
import com.generatera.authorization.oauth2.entity.OAuth2UserEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UsernamePasswordAuthenticationTokenJwtCustomizerHandler extends AbstractJwtCustomizerHandler {

	public UsernamePasswordAuthenticationTokenJwtCustomizerHandler(JwtCustomizerHandler jwtCustomizerHandler) {
		super(jwtCustomizerHandler);
	}

	@Override
	protected void customizeJwt(JwtEncodingContext jwtEncodingContext) {
		
		Authentication authentication = jwtEncodingContext.getPrincipal();
		LightningOAuth2UserDetails userPrincipal = (LightningOAuth2UserDetails)authentication.getPrincipal();
		Long userId = userPrincipal.getId();
		Set<String> authorities = userPrincipal.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.toSet());
		
		Map<String, Object> userAttributes = new HashMap<>();

		// 自定义jwt 加入 openid
		userAttributes.put("openid",userPrincipal.getOpenId());
		//userAttributes.put("userId", userId);
		//userAttributes.put("authorities", authorities); // 自定义权限集合
		
		Set<String> contextAuthorizedScopes = jwtEncodingContext.getAuthorizedScopes();
		
		JwtClaimsSet.Builder jwtClaimSetBuilder = jwtEncodingContext.getClaims();
		
		if (CollectionUtils.isEmpty(contextAuthorizedScopes)) {
			jwtClaimSetBuilder.claim(OAuth2ParameterNames.SCOPE, Collections.emptyList());
//			jwtClaimSetBuilder.claim("authorities", authorities);
		}
		
		jwtClaimSetBuilder.claims(claims ->
			userAttributes.entrySet().stream()
			.forEach(entry -> claims.put(entry.getKey(), entry.getValue()))
		);
		
	}

	@Override
	protected boolean supportCustomizeContext(Authentication authentication) {
		return authentication != null && authentication instanceof UsernamePasswordAuthenticationToken;
	}
	
}
