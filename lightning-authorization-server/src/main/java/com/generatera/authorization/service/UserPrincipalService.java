package com.generatera.authorization.service;

import com.generatera.authorization.oauth2.entity.OAuth2UserEntity;
import com.generatera.authorization.server.configure.model.ext.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;

@Validated
public interface UserPrincipalService extends UserDetailsService {

	@Override
    OAuth2UserEntity loadUserByUsername(String username);

}
