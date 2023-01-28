package com.generatera.authorization.application.server.config.token;

import org.springframework.core.convert.converter.Converter;

import java.util.HashMap;
import java.util.Map;

public final class DefaultAuthAppLevelAuthorizationTokenResponseMapConverter implements Converter<ApplicationLevelAuthorizationToken, Map<String, Object>> {
    public DefaultAuthAppLevelAuthorizationTokenResponseMapConverter() {
    }

    public Map<String, Object> convert(ApplicationLevelAuthorizationToken tokenResponse) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("access_token", tokenResponse.accessToken().getTokenValue());
        parameters.put("access_token_type", tokenResponse.accessToken().getTokenValueType().value());
        parameters.put("access_token_format", tokenResponse.accessToken().getTokenValueFormat().value());

        if (tokenResponse.refreshToken() != null) {
            parameters.put("refresh_token", tokenResponse.refreshToken().getTokenValue());
            parameters.put("refresh_token_type", tokenResponse.accessToken().getTokenValueType().value());
            parameters.put("refresh_token_format", tokenResponse.accessToken().getTokenValueFormat().value());
        }

        return parameters;
    }

}