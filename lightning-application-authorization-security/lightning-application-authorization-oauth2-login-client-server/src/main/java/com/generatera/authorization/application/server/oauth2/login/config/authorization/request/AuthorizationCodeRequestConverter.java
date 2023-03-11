package com.generatera.authorization.application.server.oauth2.login.config.authorization.request;

import com.fasterxml.jackson.core.type.TypeReference;
import com.generatera.authorization.application.server.oauth2.login.config.model.entity.AuthorizationRequestEntity;
import com.jianyue.lightning.util.JsonUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AuthorizationCodeRequestConverter implements Converter<AuthorizationRequestEntity, OAuth2AuthorizationRequest> {
    @Override
    public OAuth2AuthorizationRequest convert(AuthorizationRequestEntity source) {
        return OAuth2AuthorizationRequest.authorizationCode()
                .clientId(source.getClientId())
                .authorizationRequestUri(source.getAuthorizationRequestUri())
                .authorizationUri(source.getAuthorizationUri())
                .redirectUri(source.getRedirectUri())
                .scope(
                        Optional.ofNullable(source.getScopes())
                                .filter(StringUtils::hasText)
                                .map(
                                        ele -> JsonUtil
                                                .getDefaultJsonUtil()
                                                .fromJson(ele, new TypeReference<Set<String>>() {
                                                }))
                                .map(ele -> ele.toArray(String[]::new))
                                .orElse(null)
                )
                .state(source.getState())
                .additionalParameters(
                        Optional.ofNullable(source.getAdditionalParameters())
                                .filter(StringUtils::hasText)
                                .map(ele -> JsonUtil.getDefaultJsonUtil().fromJson(ele,
                                        new TypeReference<Map<String, Object>>() {
                                        }))
                                .orElse(null)

                )
                .attributes(
                        Optional.ofNullable(
                                        source.getAttributes()
                                )
                                .filter(StringUtils::hasText)
                                .map(ele -> JsonUtil.getDefaultJsonUtil().fromJson(ele,
                                        new TypeReference<Map<String, Object>>() {
                                        }))
                                .orElse(null)

                )
                .build();
    }
}
