package com.generatera.authorization.oauth2.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.generatera.authorization.oauth2.entity.OAuth2ClientEntity;
import com.generatera.authorization.oauth2.repository.LightningOAuth2ClientRepository;
import com.jianyue.lightning.boot.starter.util.BeanUtils;
import com.jianyue.lightning.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2TokenFormat;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ClientSettings;
import org.springframework.security.oauth2.server.authorization.config.ConfigurationSettingNames;
import org.springframework.security.oauth2.server.authorization.config.TokenSettings;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author FLJ
 * @date 2022/12/27
 * @time 17:02
 * @Description 抓取自定义实现的 已注册的客户端仓库 ..
 */
@Service
@RequiredArgsConstructor
public class LightningRegisteredClientRepository implements RegisteredClientRepository {


    private final Converter<RegisteredClient, OAuth2ClientEntity> entityConverter = new Converter<>() {
        @Override
        public OAuth2ClientEntity convert(@NotNull RegisteredClient source) {

            OAuth2ClientEntity entity = BeanUtils.transformFrom(source, OAuth2ClientEntity.class);
            assert entity != null;
            entity.setClientAuthenticationMethods(stringJoin(source.getClientAuthenticationMethods().toArray()));

            entity.setAuthorizationGrantTypes(
                    stringJoin(source.getAuthorizationGrantTypes().toArray())
            );
            entity.setRedirectUris(
                    stringJoin(source.getRedirectUris().toArray())
            );
            entity.setScopes(
                    stringJoin(source.getScopes().toArray())
            );

            // 客户端配置
            entity.setRequireAuthorizationConsent(source.getClientSettings().isRequireAuthorizationConsent());
            entity.setRequireProofKey(source.getClientSettings().isRequireProofKey());
            entity.setJwkSetUrl(source.getClientSettings().getJwkSetUrl());
            // jws algorithm ..
            entity.setTokenEndpointAuthenticationSigningAlgorithm(source.getClientSettings().getTokenEndpointAuthenticationSigningAlgorithm().getName());


            Map<String, Object> settings = source.getClientSettings().getSettings();
            HashMap<String, Object> clientOtherSettings = new HashMap<>(settings);
            // for compliant
            clientOtherSettings.remove(ConfigurationSettingNames.Client.JWK_SET_URL);
            clientOtherSettings.remove(ConfigurationSettingNames.Client.REQUIRE_PROOF_KEY);
            clientOtherSettings.remove(ConfigurationSettingNames.Client.TOKEN_ENDPOINT_AUTHENTICATION_SIGNING_ALGORITHM);
            clientOtherSettings.remove(ConfigurationSettingNames.Client.REQUIRE_AUTHORIZATION_CONSENT);

            // json 序列化
            entity.setClientOtherSettings(JsonUtil.asJSON(clientOtherSettings));


            // token 配置
            entity.setAccessTokenTime(source.getTokenSettings().getAccessTokenTimeToLive().toMillis());
            entity.setAccessTokenFormat(source.getTokenSettings().getAccessTokenFormat().getValue());
            entity.setRefreshTokenTime(source.getTokenSettings().getRefreshTokenTimeToLive().toMillis());
            entity.setIdTokenSignatureAlgorithm(source.getTokenSettings().getIdTokenSignatureAlgorithm().getName());

            Map<String, Object> tokenSettings = source.getTokenSettings().getSettings();
            HashMap<String, Object> tokenSettingsNoRequired = new HashMap<>(tokenSettings);

            // for compliant
            tokenSettingsNoRequired.remove(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT);
            tokenSettingsNoRequired.remove(ConfigurationSettingNames.Token.REUSE_REFRESH_TOKENS);
            tokenSettingsNoRequired.remove(ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE);
            tokenSettingsNoRequired.remove(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE);
            tokenSettingsNoRequired.remove(ConfigurationSettingNames.Token.ID_TOKEN_SIGNATURE_ALGORITHM);
            entity.setTokenOtherSettings(JsonUtil.asJSON(tokenSettingsNoRequired));

            return entity;
        }

        @NotNull
        private String stringJoin(@Nullable Object[] array) {
            return Optional.ofNullable(array)
                    .map(set -> StringUtils.joinWith(",", array))
                    .orElse("");
        }
    };

    private final Converter<OAuth2ClientEntity, RegisteredClient> clientConverter = new Converter<>() {
        @Override
        public RegisteredClient convert(@NotNull OAuth2ClientEntity source) {
            RegisteredClient registeredClient = BeanUtils.transformFrom(source, RegisteredClient.class);
            assert registeredClient != null;
            return RegisteredClient
                    .from(registeredClient)
                    .clientAuthenticationMethods(stringToSet(source.getClientAuthenticationMethods(), ClientAuthenticationMethod::new))
                    .authorizationGrantTypes(stringToSet(source.getAuthorizationGrantTypes(), AuthorizationGrantType::new))
                    .redirectUris(stringToSet(source.getRedirectUris(), Function.identity()))
                    .scopes(stringToSet(source.getScopes(), Function.identity()))
                    .clientSettings(
                            ClientSettings.builder()
                                    .jwkSetUrl(source.getJwkSetUrl())
                                    .requireAuthorizationConsent(source.getRequireAuthorizationConsent())
                                    .tokenEndpointAuthenticationSigningAlgorithm(jwsAlgorithmResolve(source.getTokenEndpointAuthenticationSigningAlgorithm(), true))
                                    .requireProofKey(source.getRequireProofKey())
                                    .settings(settings -> {
                                        String clientOtherSettings = source.getClientOtherSettings();
                                        Map<String, Object> values = JsonUtil.fromJson(clientOtherSettings, new TypeReference<Map<String, Object>>() {
                                        });
                                        // other settings
                                        settings.putAll(values);
                                    })
                                    .build()
                    )
                    .tokenSettings(
                            TokenSettings
                                    .builder()
                                    .accessTokenFormat(accessTokenFormatResolve(source.getAccessTokenFormat()))
                                    .accessTokenTimeToLive(Duration.ofMillis(source.getAccessTokenTime()))
                                    .idTokenSignatureAlgorithm((SignatureAlgorithm) jwsAlgorithmResolve(source.getIdTokenSignatureAlgorithm(), true))
                                    .refreshTokenTimeToLive(Duration.ofMillis(source.getRefreshTokenTime()))
                                    .reuseRefreshTokens(source.getReuseRefreshToken())
                                    .settings(settings -> {
                                        String clientOtherSettings = source.getTokenOtherSettings();
                                        Map<String, Object> values = JsonUtil.fromJson(clientOtherSettings, new TypeReference<Map<String, Object>>() {
                                        });
                                        // other settings
                                        settings.putAll(values);
                                    })
                                    .build()
                    )
                    .build();
        }

        @NotNull
        private <T> Consumer<Set<T>> stringToSet(@Nullable String value, Function<String, T> converter) {
            return set -> Optional.ofNullable(value)
                    .map(ele -> ele.split(","))
                    .ifPresent(values -> {
                        for (String s : values) {
                            set.add(converter.apply(s));
                        }
                    });
        }

        private JwsAlgorithm jwsAlgorithmResolve(String algorithm, boolean isSignature) {
            JwsAlgorithm from = SignatureAlgorithm.from(algorithm);
            if (from == null) {
                if (!isSignature) {
                    from = MacAlgorithm.from(algorithm);
                    if (from != null) {
                        return from;
                    }
                }
                throw new IllegalArgumentException("jws algorithm resolve failure ...");
            }
            return from;
        }

        private OAuth2TokenFormat accessTokenFormatResolve(String format) {
            return new OAuth2TokenFormat(format);
        }
    };


    private final LightningOAuth2ClientRepository oAuth2ClientRepository;

    @Override
    public void save(RegisteredClient registeredClient) {

        OAuth2ClientEntity entity = entityConverter.convert(registeredClient);
        assert entity != null;
        oAuth2ClientRepository.save(entity);
    }

    @Override
    public RegisteredClient findById(String id) {
        return oAuth2ClientRepository.findById(Long.parseLong(id))
                .map(clientConverter::convert)
                .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return oAuth2ClientRepository.findFirstByClientId(clientId)
                .map(clientConverter::convert)
                .orElse(null);
    }
}
