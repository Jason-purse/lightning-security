package com.generatera.authorization.application.server.oauth2.login.config.authorization.request;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.AuthorizationRequestEntity;
import com.jianyue.lightning.util.JsonUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

public class AuthorizationRequestEntityConverter implements Converter<OAuth2AuthorizationRequest, AuthorizationRequestEntity> {
    @Override
    public AuthorizationRequestEntity convert(OAuth2AuthorizationRequest source) {
        return AuthorizationRequestEntity.builder()
                .clientId(source.getClientId())
                .authorizationRequestUri(source.getAuthorizationRequestUri())
                .authorizationUri(source.getAuthorizationUri())
                .redirectUri(source.getRedirectUri())
                .scopes(JsonUtil.getDefaultJsonUtil().asJSON(source.getScopes()))
                .state(source.getState())
                .additionalParameters(JsonUtil.getDefaultJsonUtil().asJSON(source.getAdditionalParameters()))
                .attributes(JsonUtil.getDefaultJsonUtil().asJSON(source.getAttributes()))
                .responseType(source.getResponseType().getValue())
                .authorizationGrantType(source.getGrantType().getValue())
                .build();
    }
}
