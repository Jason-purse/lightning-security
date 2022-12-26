package com.generatera.authorization.oauth2.service;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.List;

public interface OAuth2RegisteredClientService {

	List<RegisteredClient> getOAuth2RegisteredClient();
	
}
