package com.generatera.authorization.application.server.oauth2.login.config.client.authorized;

import com.generatera.authorization.application.server.oauth2.login.config.client.register.ClientRegistrationEntityConverter;
import com.generatera.authorization.application.server.oauth2.login.config.model.entity.OAuthorizedClientEntity;
import com.generatera.authorization.application.server.oauth2.login.config.token.AccessTokenEntityForTokenConverter;
import com.generatera.authorization.application.server.oauth2.login.config.token.RefreshTokenEntityForTokenConverter;
import com.jianyue.lightning.util.JsonUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

import java.util.Optional;

/**
 * @author FLJ
 * @date 2023/1/9
 * @time 10:05
 * @Description 已经被授权的 OAuth2 Client 存储仓库 ..
 */
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
                        Optional.ofNullable(
                                        source.getClientRegistration()
                                )
                                .map(ele ->
                                        JsonUtil.getDefaultJsonUtil()
                                                .asJSON(
                                                        clientRegistrationEntityConverter.convert(ele)
                                                ))
                                .orElse(null)
                )
                .accessToken(
                        Optional.ofNullable(source.getAccessToken())
                                .map(ele -> JsonUtil.getDefaultJsonUtil()
                                        .asJSON(
                                                accessTokenEntityConverter.convert(ele)
                                        ))
                                .orElse(null)
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
