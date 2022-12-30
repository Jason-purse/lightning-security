package com.generatera.authorization.server.configure.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.generatera.authorization.server.configure.model.entity.OAuth2ClientEntity;
import com.generatera.authorization.server.configure.model.param.AppParam;
import com.jianyue.lightning.boot.starter.generic.crud.service.support.result.CrudResult;
import com.jianyue.lightning.boot.starter.generic.crud.service.support.validates.ValidationSupport;
import com.jianyue.lightning.boot.starter.util.dataflow.impl.InputContext;
import com.jianyue.lightning.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
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

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author FLJ
 * @date 2022/12/27
 * @time 17:02
 * @Description 抓取自定义实现的 已注册的客户端仓库 ..
 */
@RequiredArgsConstructor
public class LightningRegisteredClientRepository implements RegisteredClientRepository {

    private final Converter<RegisteredClient, OAuth2ClientEntity> entityConverter = new Converter<>() {
        @Override
        public OAuth2ClientEntity convert(@NotNull RegisteredClient source) {

            OAuth2ClientEntity entity = new OAuth2ClientEntity();
            entity.setId(Long.parseLong(source.getId()));
            entity.setClientId(source.getClientId());
            entity.setClientName(source.getClientName());
            entity.setClientSecret(source.getClientSecret());
            entity.setClientIdIssuedAt(source.getClientIdIssuedAt());
            entity.setClientSecretExpiresAt(source.getClientSecretExpiresAt());
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
            if (ObjectUtils.isNotEmpty(source.getClientSettings().getTokenEndpointAuthenticationSigningAlgorithm())) {
                // jws algorithm ..
                entity.setTokenEndpointAuthenticationSigningAlgorithm(source.getClientSettings().getTokenEndpointAuthenticationSigningAlgorithm().getName());
            }

            Map<String, Object> settings = source.getClientSettings().getSettings();
            HashMap<String, Object> clientOtherSettings = new HashMap<>(settings);
            // for compliant
            clientOtherSettings.remove(ConfigurationSettingNames.Client.JWK_SET_URL);
            clientOtherSettings.remove(ConfigurationSettingNames.Client.REQUIRE_PROOF_KEY);
            clientOtherSettings.remove(ConfigurationSettingNames.Client.TOKEN_ENDPOINT_AUTHENTICATION_SIGNING_ALGORITHM);
            clientOtherSettings.remove(ConfigurationSettingNames.Client.REQUIRE_AUTHORIZATION_CONSENT);

            // json 序列化
            entity.setClientOtherSettings(JsonUtil.getDefaultJsonUtil().asJSON(clientOtherSettings));


            // token 配置
            if (ObjectUtils.isNotEmpty(source.getTokenSettings().getAccessTokenTimeToLive())) {
                entity.setAccessTokenTime(source.getTokenSettings().getAccessTokenTimeToLive().toMillis());
            }
            if (ObjectUtils.isNotEmpty(source.getTokenSettings().getAccessTokenTimeToLive())) {
                entity.setAccessTokenFormat(source.getTokenSettings().getAccessTokenFormat().getValue());
            }
            if (ObjectUtils.isNotEmpty(source.getTokenSettings().getAccessTokenTimeToLive())) {
                entity.setRefreshTokenTime(source.getTokenSettings().getRefreshTokenTimeToLive().toMillis());
            }
            if (ObjectUtils.isNotEmpty(source.getTokenSettings().getAccessTokenTimeToLive())) {
                entity.setIdTokenSignatureAlgorithm(source.getTokenSettings().getIdTokenSignatureAlgorithm().getName());
            }


            Map<String, Object> tokenSettings = source.getTokenSettings().getSettings();
            HashMap<String, Object> tokenSettingsNoRequired = new HashMap<>(tokenSettings);

            // for compliant
            tokenSettingsNoRequired.remove(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT);
            tokenSettingsNoRequired.remove(ConfigurationSettingNames.Token.REUSE_REFRESH_TOKENS);
            tokenSettingsNoRequired.remove(ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE);
            tokenSettingsNoRequired.remove(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE);
            tokenSettingsNoRequired.remove(ConfigurationSettingNames.Token.ID_TOKEN_SIGNATURE_ALGORITHM);
            entity.setTokenOtherSettings(JsonUtil.getDefaultJsonUtil().asJSON(tokenSettingsNoRequired));

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

            RegisteredClient.Builder builder = RegisteredClient.withId(source.getId().toString())
                    .clientId(source.getClientId())
                    .clientName(source.getClientName())
                    .clientIdIssuedAt(source.getClientIdIssuedAt())
                    .clientSecretExpiresAt(source.getClientSecretExpiresAt())
                    .clientSecret(source.getClientSecret())
                    .clientAuthenticationMethods(stringToSet(source.getClientAuthenticationMethods(), ClientAuthenticationMethod::new))
                    .authorizationGrantTypes(stringToSet(source.getAuthorizationGrantTypes(), AuthorizationGrantType::new))
                    .redirectUris(stringToSet(source.getRedirectUris(), Function.identity()))
                    .scopes(stringToSet(source.getScopes(), Function.identity()));


            ClientSettings.Builder clientSettingsBuilder = ClientSettings.builder();
            if (StringUtils.isNotBlank(source.getJwkSetUrl())) {
                clientSettingsBuilder.jwkSetUrl(source.getJwkSetUrl());
            }
            if (ObjectUtils.isNotEmpty(source.getRequireAuthorizationConsent())) {
                clientSettingsBuilder.requireAuthorizationConsent(source.getRequireAuthorizationConsent());
            }
            if (StringUtils.isNotBlank(source.getTokenEndpointAuthenticationSigningAlgorithm())) {
                clientSettingsBuilder.tokenEndpointAuthenticationSigningAlgorithm(jwsAlgorithmResolve(source.getTokenEndpointAuthenticationSigningAlgorithm(), true));
            }
            if (ObjectUtils.isNotEmpty(source.getRequireProofKey())) {
                clientSettingsBuilder.requireProofKey(source.getRequireProofKey());
            }

            TokenSettings.Builder tokenSettingsBuilder = TokenSettings
                    .builder();
            if (ObjectUtils.isNotEmpty(source.getAccessTokenFormat())) {
                tokenSettingsBuilder
                        .accessTokenFormat(accessTokenFormatResolve(source.getAccessTokenFormat()));
            }
            if (ObjectUtils.isNotEmpty(source.getAccessTokenTime())) {
                tokenSettingsBuilder.accessTokenTimeToLive(Duration.ofMillis(source.getAccessTokenTime()));
            }

            if (ObjectUtils.isNotEmpty(source.getIdTokenSignatureAlgorithm())) {
                tokenSettingsBuilder.idTokenSignatureAlgorithm((SignatureAlgorithm) jwsAlgorithmResolve(source.getIdTokenSignatureAlgorithm(), true));
            }

            if (ObjectUtils.isNotEmpty(source.getRefreshTokenTime())) {
                tokenSettingsBuilder.refreshTokenTimeToLive(Duration.ofMillis(source.getRefreshTokenTime()));

            }
            if (ObjectUtils.isNotEmpty(source.getReuseRefreshToken())) {
                tokenSettingsBuilder.reuseRefreshTokens(source.getReuseRefreshToken());
            }


            return builder
                    .clientSettings(
                            clientSettingsBuilder.settings(settings -> {
                                        String clientOtherSettings = source.getClientOtherSettings();
                                        Map<String, Object> values = JsonUtil.getDefaultJsonUtil().fromJson(clientOtherSettings, new TypeReference<Map<String, Object>>() {
                                        });
                                        // other settings
                                        settings.putAll(values);
                                    })
                                    .build()
                    )
                    .tokenSettings(
                            tokenSettingsBuilder.settings(settings -> {
                                        String clientOtherSettings = source.getTokenOtherSettings();
                                        Map<String, Object> values = JsonUtil.getDefaultJsonUtil().fromJson(clientOtherSettings, new TypeReference<Map<String, Object>>() {
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
                            set.add(converter.apply(s.trim()));
                        }
                    });
        }

        private JwsAlgorithm jwsAlgorithmResolve(String algorithm, boolean isSignature) {
            if (StringUtils.isBlank(algorithm)) {
                return null;
            }
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

    private final AppService oAuth2ClientRepository;

    @Override
    public void save(RegisteredClient registeredClient) {

        OAuth2ClientEntity entity = entityConverter.convert(registeredClient);
        assert entity != null;

        if (entity.getId() != null) {
            oAuth2ClientRepository.getDbTemplate().update(entity);
        } else {
            oAuth2ClientRepository.getDbTemplate().add(entity);
        }

    }

    @Override
    public RegisteredClient findById(String id) {
        AppParam appParam = new AppParam();
        appParam.setId(Long.parseLong(id));
        CrudResult crudResult = oAuth2ClientRepository.selectOperationById(InputContext.of(appParam));
        if(crudResult.hasResult()) {
            assert crudResult.getResult() != null;
            return clientConverter.convert((OAuth2ClientEntity) crudResult.getResult());
        }
        return null;
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        AppParam appParam = new AppParam();
        appParam.setClientId(clientId);
        // select 
        ValidationSupport.Companion.setSelectListGroup();
        CrudResult crudResult = oAuth2ClientRepository.selectOperation(InputContext.of(appParam));
        if(crudResult.hasResults()) {
            assert crudResult.getResult() != null;
            Collection<?> result = (Collection<?>) crudResult.getResult();
            OAuth2ClientEntity next = ((OAuth2ClientEntity) result.iterator().next());
            ValidationSupport.Companion.removeValidationGroupAndReturnOld();
            return clientConverter.convert(next );
        }
        ValidationSupport.Companion.removeValidationGroupAndReturnOld();
        return null;
    }
}
