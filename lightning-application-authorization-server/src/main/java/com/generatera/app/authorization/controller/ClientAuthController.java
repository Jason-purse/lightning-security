package com.generatera.app.authorization.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/client/auth")
public class ClientAuthController {



    @GetMapping
    public String clientAuth(Authentication authentication,@RegisteredOAuth2AuthorizedClient("generatera") OAuth2AuthorizedClient authorizedClient) {



        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();

        System.out.println(accessToken);

        return accessToken.getTokenValue();
    }
}
