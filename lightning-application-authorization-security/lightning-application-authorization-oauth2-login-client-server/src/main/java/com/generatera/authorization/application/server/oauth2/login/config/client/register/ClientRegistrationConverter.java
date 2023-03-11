package com.generatera.authorization.application.server.oauth2.login.config.client.register;

import com.fasterxml.jackson.core.type.TypeReference;
import com.generatera.authorization.application.server.oauth2.login.config.model.entity.client.info.ClientRegistrationEntity;
import com.jianyue.lightning.util.JsonUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ClientRegistrationConverter implements Converter<ClientRegistrationEntity, ClientRegistration> {
    @Override
    public ClientRegistration convert(ClientRegistrationEntity source) {
        return ClientRegistration
                .withRegistrationId(source.getRegistrationId())
                .clientId(source.getClientId())
                .clientSecret(source.getClientSecret())
                .clientName(source.getClientName())
                .authorizationGrantType(new AuthorizationGrantType(source.getAuthorizationGrantType()))
                .clientAuthenticationMethod(new ClientAuthenticationMethod(source.getClientAuthenticationMethod()))
                .scope(
                        Optional.ofNullable(source.getScopes())
                                .filter(StringUtils::hasText)
                                .map(ele ->  {
                                    String[] split = ele.split(",");
                                    List<String> strings = new LinkedList<>();
                                    for (String s : split) {
                                        if(StringUtils.hasText(s)) {
                                            strings.add(s);
                                        }
                                    }
                                    return strings;
                                }).orElse(Collections.emptyList())
                )
                .redirectUri(source.getRedirectUri())

                // provider Details 必须提供 ..
                .issuerUri(source.getIssuerUri())
                .jwkSetUri(source.getJwkSetUri())
                .authorizationUri(source.getAuthorizationUri())
                .tokenUri(source.getTokenUri())
                .userInfoUri(source.getUserInfoUri())
                .userInfoAuthenticationMethod(new AuthenticationMethod(source.getUserInfoAuthenticationMethod()))
                .userNameAttributeName(source.getUserNameAttributeName())
                .providerConfigurationMetadata (
                        JsonUtil.getDefaultJsonUtil().fromJson(source.getConfigurationMetadata(), new TypeReference<>() {
                        })
                )
                .build();
    }
}
