package com.generatera.resource.server.specification.token.jwt.config;

import com.generatera.resource.server.config.token.LightningAuthenticationToken;
import org.springframework.util.Assert;

import java.util.Collections;

public class JwtAuthenticationToken extends LightningAuthenticationToken {

    private final String token;

    public JwtAuthenticationToken(String token) {
        super(Collections.emptyList());
        Assert.hasText(token, "token cannot be empty");
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }

    public Object getCredentials() {
        return this.getToken();
    }

    public Object getPrincipal() {
        return this.getToken();
    }
}
