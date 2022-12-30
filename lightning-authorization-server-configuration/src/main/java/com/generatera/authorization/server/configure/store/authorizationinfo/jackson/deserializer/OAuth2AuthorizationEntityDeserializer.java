package com.generatera.authorization.server.configure.store.authorizationinfo.jackson.deserializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.generatera.authorization.server.configure.store.authorizationinfo.OAuth2AuthorizationEntity;
import com.jianyue.lightning.util.JsonUtil;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.io.IOException;
import java.util.Map;

/**
 * @author FLJ
 * @date 2022/12/30
 * @time 10:07
 * @Description oauth2 AuthorizationEntity 的反序列化处理
 */
public class OAuth2AuthorizationEntityDeserializer extends StdDeserializer<OAuth2AuthorizationEntity> {
    public OAuth2AuthorizationEntityDeserializer() {
        super(OAuth2AuthorizationEntity.class);
    }

    @Override
    public OAuth2AuthorizationEntity deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode jsonNode = deserializationContext.readTree(jsonParser);
        String id = jsonNode.get("id").asText();
        String registeredClientId = jsonNode.get("registeredClientId").asText();
        String principalName = jsonNode.get("principalName").asText();
        String authorizationGrantType = jsonNode.get("authorizationGrantType").asText();
        AuthorizationGrantType grantType = new AuthorizationGrantType(authorizationGrantType);

        JsonNode accessToken = jsonNode.get("accessToken");
        JsonNode token = accessToken.get("token");
        JsonNode access_metadata = accessToken.get("metadata");
        JavaType javaType = JsonUtil.getDefaultJsonUtil().createJavaType(Map.class, String.class, Object.class);
        Map<String, Object> metaMap = deserializationContext.readTreeAsValue(access_metadata, javaType);
        // 访问token不为空
        OAuth2AccessToken oAuth2AccessToken = deserializationContext.readTreeAsValue(token, OAuth2AccessToken.class);
        OAuth2Authorization.Builder builder = OAuth2Authorization
                .withRegisteredClient(RegisteredClient.withId(registeredClientId).build())
                // 访问  token
                .token(oAuth2AccessToken, metadata -> metadata.putAll(metaMap));

        JsonNode refreshToken = jsonNode.get("refreshToken");
        if (!refreshToken.isNull()) {
            JsonNode refresh_token = refreshToken.get("token");
            JsonNode refresh_metadata = refresh_token.get("metadata");
            Map<String, Object> refresh_metaMap = deserializationContext.readTreeAsValue(access_metadata, javaType);
            builder.token(deserializationContext.readTreeAsValue(refresh_token, OAuth2RefreshToken.class), metadata -> metadata.putAll(
                    refresh_metaMap));
        }
        JsonNode oidcToken = jsonNode.get("oidcToken");
        if(!oidcToken.isNull()) {
            JsonNode oidc_token = oidcToken.get("token");
            JsonNode oidc_metadata = oidcToken.get("metadata");
            Map<String, Object> refresh_metaMap = deserializationContext.readTreeAsValue(access_metadata, javaType);
            builder.token(deserializationContext.readTreeAsValue(oidc_token, OidcIdToken.class), metadata -> metadata.putAll(
                    refresh_metaMap));
        }

        JsonNode authorizationCodeToken = jsonNode.get("authorizationCodeToken");
        if(!authorizationCodeToken.isNull()) {
            JsonNode authorizationCode_token = authorizationCodeToken.get("token");
            JsonNode authorizationCode_metadata = authorizationCodeToken.get("metadata");
            Map<String, Object> refresh_metaMap = deserializationContext.readTreeAsValue(access_metadata, javaType);
            builder.token(deserializationContext.readTreeAsValue(authorizationCode_token, OAuth2AuthorizationCode.class), metadata -> metadata.putAll(
                    refresh_metaMap));
        }

        JsonNode attributes = jsonNode.get("attributes");


        return OAuth2AuthorizationEntity.builder()
                .id(id)
                .authorizationGrantType(grantType)
                //.authorizationCodeToken()
                .build();
    }
}
