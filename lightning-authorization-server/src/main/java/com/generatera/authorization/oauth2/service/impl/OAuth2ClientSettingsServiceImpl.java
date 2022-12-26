package com.generatera.authorization.oauth2.service.impl;

import com.generatera.authorization.oauth2.entity.OAuth2ClientSetting;
import com.generatera.authorization.oauth2.service.OAuth2ClientSettingsService;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.stereotype.Service;

@Service
public class OAuth2ClientSettingsServiceImpl implements OAuth2ClientSettingsService {

	@Override
	public ClientSettings getClientSettings(OAuth2ClientSetting clientSetting) {
		if (clientSetting == null) {
			return null;
		}
		boolean requireAuthorizationConsent = clientSetting.isRequireAuthorizationConsent();
		ClientSettings clientSettings = ClientSettings.builder().requireAuthorizationConsent(requireAuthorizationConsent).build();
		return clientSettings;
	}

}
