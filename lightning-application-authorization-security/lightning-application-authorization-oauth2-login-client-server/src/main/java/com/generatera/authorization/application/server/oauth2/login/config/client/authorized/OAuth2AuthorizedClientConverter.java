package com.generatera.authorization.application.server.oauth2.login.config.client.authorized;

import com.generatera.authorization.application.server.oauth2.login.config.client.register.ClientRegistrationConverter;
import com.generatera.authorization.application.server.oauth2.login.config.model.entity.registration.ClientRegistrationEntity;
import com.generatera.authorization.application.server.oauth2.login.config.model.entity.OAuth2AccessTokenEntity;
import com.generatera.authorization.application.server.oauth2.login.config.model.entity.OAuth2RefreshTokenEntity;
import com.generatera.authorization.application.server.oauth2.login.config.model.entity.OAuthorizedClientEntity;
import com.generatera.authorization.application.server.oauth2.login.config.token.AccessTokenConverter;
import com.generatera.authorization.application.server.oauth2.login.config.token.RefreshTokenConverter;
import com.jianyue.lightning.util.JsonUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.util.Objects;

public class OAuth2AuthorizedClientConverter implements Converter<OAuthorizedClientEntity, OAuth2AuthorizedClient> {

    private final ClientRegistrationConverter clientRegistrationConverter = new ClientRegistrationConverter();

    private final AccessTokenConverter accessTokenConverter = new AccessTokenConverter();

    private final RefreshTokenConverter refreshTokenConverter = new RefreshTokenConverter();

    @Override
    public OAuth2AuthorizedClient convert(@NonNull OAuthorizedClientEntity source) {
        OAuth2AccessTokenEntity entity =
                JsonUtil.getDefaultJsonUtil()
                        .fromJson(source.getAccessToken(), OAuth2AccessTokenEntity.class);
        OAuth2AccessToken accessToken = accessTokenConverter.convert(entity);
        assert accessToken != null;
        return new OAuth2AuthorizedClient(
                // must not be null
                Objects.requireNonNull(
                        clientRegistrationConverter.convert(
                                JsonUtil
                                        .getDefaultJsonUtil()
                                        .fromJson(
                                                source.getClientRegistration(),
                                                ClientRegistrationEntity.class
                                        )
                        )),
                source.getPrincipalName(),
                accessToken,
                refreshTokenConverter.convert(
                        JsonUtil
                                .getDefaultJsonUtil()
                                .fromJson(source.getRefreshToken(), OAuth2RefreshTokenEntity.class)
                )
        );

    }
}
