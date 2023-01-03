package com.generatera.authorization.server.common.configuration.model.ext;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<Long> {

	/**
	 * 获取token里面的用户id(userId)
	 */
	@Override
	public Optional<Long> getCurrentAuditor() {
		
		Long userId = 0L;
		
		Authentication principal = SecurityContextHolder.getContext().getAuthentication();
		if (isPrincipalAuthenticated(principal)) {
			UserPrincipal userPrincipal = (UserPrincipal) principal.getPrincipal();
			userId = userPrincipal.getId();
		}
		
		return Optional.of(userId);
	}
	
	private static boolean isPrincipalAuthenticated(Authentication principal) {
		return principal != null &&
				!AnonymousAuthenticationToken.class.isAssignableFrom(principal.getClass()) && principal.isAuthenticated();
	}

}
