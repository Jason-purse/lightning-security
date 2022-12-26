package com.generatera.authorization.oauth2.service;

import com.generatera.authorization.oauth2.entity.OAuth2ClientSetting;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

public interface OAuth2ClientSettingsService {

	ClientSettings getClientSettings(OAuth2ClientSetting clientSetting);
	
}
