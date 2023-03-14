package com.generatera.test.auth.server.service;

import com.generatera.authorization.application.server.form.login.config.components.LightningUserDetailService;
import com.generatera.test.auth.server.entity.LightningOAuth2UserDetails;
import org.springframework.validation.annotation.Validated;

@Validated
public interface UserPrincipalService extends LightningUserDetailService {

	@Override
    LightningOAuth2UserDetails loadUserByUsername(String username);

}
