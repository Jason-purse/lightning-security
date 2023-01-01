package com.generatera.authorization.application.server.oauth2.login.config.client.oauthorized;

import com.generatera.authorization.application.server.oauth2.login.config.client.ClientRegistrationEntityConverter;
import com.generatera.authorization.application.server.oauth2.login.config.model.entity.OAuthorizedClientEntity;
import com.generatera.authorization.application.server.oauth2.login.config.token.AccessTokenEntityForTokenConverter;
import com.generatera.authorization.application.server.oauth2.login.config.token.RefreshTokenEntityForTokenConverter;
import com.jianyue.lightning.util.JsonUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

import java.util.Optional;

public class OAuth2AuthorizedClientEntityConverter implements Converter<OAuth2AuthorizedClient, OAuthorizedClientEntity> {

    private final ClientRegistrationEntityConverter clientRegistrationEntityConverter
            = new ClientRegistrationEntityConverter();

    private final AccessTokenEntityForTokenConverter accessTokenEntityConverter = new AccessTokenEntityForTokenConverter();

    private final RefreshTokenEntityForTokenConverter refreshTokenEntityConverter = new RefreshTokenEntityForTokenConverter();

    @Override
    public OAuthorizedClientEntity convert(@NonNull OAuth2AuthorizedClient source) {

        return OAuthorizedClientEntity
                .builder()
                .clientRegistrationId(source.getClientRegistration().getRegistrationId())
                .clientRegistration(
                        JsonUtil.getDefaultJsonUtil()
                                .asJSON(clientRegistrationEntityConverter.convert(source.getClientRegistration()))
                )
                .accessToken(
                        JsonUtil.getDefaultJsonUtil()
                                .asJSON(
                                        accessTokenEntityConverter.convert(source.getAccessToken())
                                )
                )
                .refreshToken(
                        Optional.ofNullable(
                                        source.getRefreshToken()
                                )
                                .map(ele ->
                                        JsonUtil.getDefaultJsonUtil()
                                                .asJSON(
                                                        refreshTokenEntityConverter.convert(ele)
                                                )
                                )
                                .orElse(null)

                )
                .principalName(source.getPrincipalName())
                .build();

    }
}
