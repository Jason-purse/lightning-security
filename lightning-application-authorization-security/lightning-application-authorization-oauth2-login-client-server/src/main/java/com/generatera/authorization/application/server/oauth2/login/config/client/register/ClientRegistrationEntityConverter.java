package com.generatera.authorization.application.server.oauth2.login.config.client.register;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.client.info.ClientRegistrationEntity;
import com.jianyue.lightning.util.JsonUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.Optional;

public class ClientRegistrationEntityConverter implements Converter<ClientRegistration, ClientRegistrationEntity> {
    @Override
    public ClientRegistrationEntity convert(@NonNull ClientRegistration source) {

        return ClientRegistrationEntity.builder()
                .registrationId(source.getRegistrationId())
                .clientAuthenticationMethod(source.getClientAuthenticationMethod().getValue())
                .authorizationUri(source.getProviderDetails().getAuthorizationUri())
                .authorizationGrantType(source.getAuthorizationGrantType().getValue())
                .clientName(source.getClientName())
                .clientId(source.getClientId())
                .clientSecret(source.getClientSecret())
                .redirectUri(source.getRedirectUri())
                .scopes(
                        Optional.ofNullable(source.getScopes())
                                .filter(ObjectUtils::isNotEmpty)
                                .map(ele ->
                                        StringUtils.joinWith(",", ele.toArray())
                                )
                                .orElse("")
                )
                .issuerUri(source.getProviderDetails().getIssuerUri())
                .userNameAttributeName(source.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName())
                .userInfoAuthenticationMethod(
                        source.getProviderDetails().getUserInfoEndpoint().getAuthenticationMethod().getValue()
                )
                .userInfoUri(source.getProviderDetails().getUserInfoEndpoint().getUri())
                .jwkSetUri(source.getProviderDetails().getJwkSetUri())
                .tokenUri(source.getProviderDetails().getTokenUri())
                .configurationMetadata(
                        JsonUtil.getDefaultJsonUtil().asJSON(source.getProviderDetails().getConfigurationMetadata())
                )
                .build();
    }
}
