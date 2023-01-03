package com.generatera.authorization.service;

import com.generatera.authorization.model.entity.LightningOAuth2UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;

@Validated
public interface UserPrincipalService extends UserDetailsService {

	@Override
    LightningOAuth2UserDetails loadUserByUsername(String username);

}
