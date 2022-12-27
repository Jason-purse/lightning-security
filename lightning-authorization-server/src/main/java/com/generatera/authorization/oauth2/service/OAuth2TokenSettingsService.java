package com.generatera.authorization.oauth2.service;

import com.generatera.authorization.oauth2.entity.OAuth2ClientTokenSetting;
import org.springframework.security.oauth2.server.authorization.config.TokenSettings;

public interface OAuth2TokenSettingsService {

	TokenSettings getTokenSettings(OAuth2ClientTokenSetting clientTokenSetting);
	
}
