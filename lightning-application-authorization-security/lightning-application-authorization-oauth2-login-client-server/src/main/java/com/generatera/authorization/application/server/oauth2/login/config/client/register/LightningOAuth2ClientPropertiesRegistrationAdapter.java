package com.generatera.authorization.application.server.oauth2.login.config.client.register;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.registration.ClientRegistrationEntity;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties.Provider;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.core.convert.ConversionException;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration.Builder;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.StringUtils;

import java.util.*;

public final class LightningOAuth2ClientPropertiesRegistrationAdapter {
    private LightningOAuth2ClientPropertiesRegistrationAdapter() {
    }

    public static Map<String, ClientRegistration> getClientRegistrations(List<ClientRegistrationEntity> entities) {
        Map<String, ClientRegistration> clientRegistrations = new HashMap<>();
        entities.forEach((value) -> {

            Provider provider = new Provider();
            provider.setAuthorizationUri(value.getAuthorizationUri());
            // 设置 issuerUri
            provider.setIssuerUri(value.getIssuerUri());
            provider.setJwkSetUri(value.getJwkSetUri());
            provider.setTokenUri(value.getTokenUri());
            provider.setUserInfoUri(value.getUserInfoUri());
            provider.setUserInfoAuthenticationMethod(value.getUserInfoAuthenticationMethod());
            provider.setUserNameAttribute(value.getUserNameAttributeName());
            ClientRegistration clientRegistration = getClientRegistration(value.getRegistrationId(), value,
                    provider);
            clientRegistrations.put(value.getRegistrationId(), clientRegistration);

            clientRegistrations.put(value.getRegistrationId(), clientRegistration);
        });
        return clientRegistrations;
    }

    private static ClientRegistration getClientRegistration(String registrationId, ClientRegistrationEntity entity, Provider provider) {
        Builder builder = getBuilderFromIssuerIfPossible(registrationId, provider);

        if (builder == null) {
            builder = getBuilder(registrationId,entity.getProviderName());
        }

        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(entity::getClientId).to(builder::clientId);
        map.from(entity::getClientSecret).to(builder::clientSecret);
        map.from(entity::getClientAuthenticationMethod).as(ClientAuthenticationMethod::new).to(builder::clientAuthenticationMethod);
        map.from(entity::getAuthorizationGrantType).as(AuthorizationGrantType::new).to(builder::authorizationGrantType);
        map.from(entity::getRedirectUri).to(builder::redirectUri);
        map.from(Optional.of(entity.getScopes()).filter(StringUtils::hasText).map(ele -> Arrays.asList(ele.split(","))).orElse(Collections.emptyList()))
                .as(StringUtils::toStringArray).to(builder::scope);
        map.from(entity::getClientName).to(builder::clientName);
        return builder.build();
    }

    private static Builder getBuilderFromIssuerIfPossible(String registrationId, Provider provider) {
        String issuer = provider.getIssuerUri();
        if (StringUtils.hasText(issuer)) {
            Builder builder = ClientRegistrations.fromIssuerLocation(issuer).registrationId(registrationId);
            return getBuilder(builder, provider);
        }

        Builder builder = getBuilder(ClientRegistration.withRegistrationId(registrationId), provider);
        try {
            // 如果能配置成功
            builder.build();
            return builder;
        } catch (Exception e) {
            // 如果存在缺失数据 ... 退出 ...
            // 例如它可能是常用provider ....(CommonProvider的配置) ...

            for (CommonOAuth2Provider value : CommonOAuth2Provider.values()) {
                if (value.name().equalsIgnoreCase(registrationId)) {
                    return null;
                }
            }

            throw new IllegalStateException(getErrorMessage(registrationId) + "configure the registrationId or provider configuration or providerName correctly !!");
        }

    }

    private static Builder getBuilder(String providerId,String providerName) {
        providerId = StringUtils.hasText(providerName) ? providerName : providerId;
        CommonOAuth2Provider provider = getCommonProvider(providerId);
        if (provider == null) {
            throw new IllegalStateException(getErrorMessage(providerId));
        } else {
            return provider.getBuilder(providerId);
        }
    }

    private static String getErrorMessage(String registrationId) {
        return "Unknown provider ID '" + registrationId + " ,No corresponding provider configuration found !!";
    }

    private static Builder getBuilder(Builder builder, Provider provider) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(provider::getAuthorizationUri).to(builder::authorizationUri);
        map.from(provider::getTokenUri).to(builder::tokenUri);
        map.from(provider::getUserInfoUri).to(builder::userInfoUri);
        map.from(provider::getUserInfoAuthenticationMethod).as(AuthenticationMethod::new).to(builder::userInfoAuthenticationMethod);
        map.from(provider::getJwkSetUri).to(builder::jwkSetUri);
        map.from(provider::getUserNameAttribute).to(builder::userNameAttributeName);
        return builder;
    }

    private static CommonOAuth2Provider getCommonProvider(String providerId) {
        try {
            return (CommonOAuth2Provider) ApplicationConversionService.getSharedInstance().convert(providerId, CommonOAuth2Provider.class);
        } catch (ConversionException var2) {
            return null;
        }
    }
}