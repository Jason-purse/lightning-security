package com.generatera.authorization.application.server.oauth2.login.config.client.register;

import com.fasterxml.jackson.core.type.TypeReference;
import com.generatera.authorization.application.server.oauth2.login.config.model.entity.registration.ClientRegistrationEntity;
import com.jianyue.lightning.util.JsonUtil;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesRegistrationAdapter;
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
/**
 * @author FLJ
 * @date 2023/2/2
 * @time 15:15
 * @Description 废弃
 */
@Deprecated
public class ClientRegistrationConverter implements Converter<ClientRegistrationEntity, ClientRegistration> {
    @Override
    public ClientRegistration convert(ClientRegistrationEntity source) {
        ClientRegistration.Builder builder = ClientRegistration
                .withRegistrationId(source.getRegistrationId())
                .clientId(source.getClientId())
                .clientSecret(source.getClientSecret())
                .clientName(source.getClientName())
                .authorizationGrantType(new AuthorizationGrantType(source.getAuthorizationGrantType()))
                .clientAuthenticationMethod(new ClientAuthenticationMethod(source.getClientAuthenticationMethod()))
                .scope(
                        Optional.ofNullable(source.getScopes())
                                .filter(StringUtils::hasText)
                                .map(ele -> {
                                    String[] split = ele.split(",");
                                    List<String> strings = new LinkedList<>();
                                    for (String s : split) {
                                        if (StringUtils.hasText(s)) {
                                            strings.add(s);
                                        }
                                    }
                                    return strings;
                                }).orElse(Collections.emptyList())
                )
                .redirectUri(source.getRedirectUri());
        builder

                // provider Details 必须提供 ..
                .issuerUri(source.getIssuerUri())
                .jwkSetUri(source.getJwkSetUri())
                .authorizationUri(source.getAuthorizationUri())
                .tokenUri(source.getTokenUri())
                .userInfoUri(source.getUserInfoUri())
                .userInfoAuthenticationMethod(StringUtils.hasText(source.getUserInfoAuthenticationMethod()) ? new AuthenticationMethod(source.getUserInfoAuthenticationMethod()) : null)
                .userNameAttributeName(source.getUserNameAttributeName());
        if (StringUtils.hasText(source.getConfigurationMetadata())) {
            builder.providerConfigurationMetadata(
                    JsonUtil.getDefaultJsonUtil().fromJson(source.getConfigurationMetadata(), new TypeReference<>() {
                    })
            );
        }

        return builder.build();
    }
}
