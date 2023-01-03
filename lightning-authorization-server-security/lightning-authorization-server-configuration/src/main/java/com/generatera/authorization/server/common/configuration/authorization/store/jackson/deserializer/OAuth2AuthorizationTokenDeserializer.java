package com.generatera.authorization.server.common.configuration.authorization.store.jackson.deserializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.jianyue.lightning.util.JsonUtil;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.generatera.authorization.server.common.configuration.model.entity.RedisOAuth2AuthorizationEntity.*;


/**
 * @author FLJ
 * @date 2022/12/30
 * @time 10:51
 * @Description OAuth2Token Deserialize ...
 */
public class OAuth2AuthorizationTokenDeserializer extends StdDeserializer<OAuth2Authorization.Token<?>> {
    public OAuth2AuthorizationTokenDeserializer() {
        super(OAuth2Authorization.Token.class);
    }

    @Override
    public OAuth2Authorization.Token<?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode jsonNode = deserializationContext.readTree(jsonParser);
        JsonStreamContext parsingContext = jsonParser.getParsingContext();
        String currentName = parsingContext.getCurrentName();
        if (ACCESS_TOKEN_FIELD_NAME.equals(currentName)) {
            return resolveToken(jsonNode, deserializationContext, accessToken -> {
                String tokenValue = accessToken.get("tokenValue").asText();
                String issuedAt = accessToken.get("issuedAt").asText();
                String expiresAt = accessToken.get("expiresAt").asText();
                Set<String> scopes = JsonUtil.getDefaultJsonUtil().fromJson(accessToken.get("scopes").toString(), JsonUtil.getDefaultJsonUtil().createJavaType(Set.class, String.class));
                return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, tokenValue, JsonUtil.getDefaultJsonUtil().fromJson(issuedAt, Instant.class),
                        JsonUtil.getDefaultJsonUtil().fromJson(expiresAt, Instant.class), scopes);
            });
        }

        if (REFRESH_TOKEN_FIELD_NAME.equals(currentName)) {
            return resolveToken(jsonNode, deserializationContext, token -> {
                String tokenValue = token.get("tokenValue").asText();
                String issuedAt = token.get("issuedAt").asText();
                String expiresAt = token.get("expiresAt").asText();
                return new OAuth2RefreshToken(tokenValue, JsonUtil.getDefaultJsonUtil().fromJson(issuedAt, Instant.class),
                        JsonUtil.getDefaultJsonUtil().fromJson(expiresAt, Instant.class));
            });
        }

        if (OIDC_TOKEN_FIELD_NAME.equals(currentName)) {
            return resolveToken(jsonNode, deserializationContext, token -> {
                String tokenValue = token.get("tokenValue").asText();
                String issuedAt = token.get("issuedAt").asText();
                String expiresAt = token.get("expiresAt").asText();
                JsonNode claims = token.get("claims");
                Map<String, Object> claimsMap = Collections.emptyMap();
                if (!claims.isNull()) {
                    claimsMap = JsonUtil.getDefaultJsonUtil().fromJson(claims.asText(), new TypeReference<Map<String, Object>>() {
                    });
                }
                return new OidcIdToken(tokenValue, JsonUtil.getDefaultJsonUtil().fromJson(issuedAt, Instant.class),
                        JsonUtil.getDefaultJsonUtil().fromJson(expiresAt, Instant.class), claimsMap);
            });
        }

        if (AUTHORIZATION_CODE_TOKEN_FIELD_NAME.equals(currentName)) {
            return resolveToken(jsonNode, deserializationContext, token -> {
                String tokenValue = token.get("tokenValue").asText();
                String issuedAt = token.get("issuedAt").asText();
                String expiresAt = token.get("expiresAt").asText();
                return new OAuth2AuthorizationCode(tokenValue, JsonUtil.getDefaultJsonUtil().fromJson(issuedAt, Instant.class),
                        JsonUtil.getDefaultJsonUtil().fromJson(expiresAt, Instant.class));
            });
        }

        // otherwise
        return null;
    }

    private OAuth2Authorization.Token<?> resolveToken(JsonNode jsonNode, DeserializationContext deserializationContext, Function<JsonNode, OAuth2Token> tokenGenerator) throws IOException {
        JsonNode token = jsonNode.get("token");
        JsonNode access_metadata = jsonNode.get("metadata");
        JavaType javaType = JsonUtil.getDefaultJsonUtil().createJavaType(Map.class, String.class, Object.class);
        Map<String, Object> metaMap = deserializationContext.readTreeAsValue(access_metadata, javaType);

        OAuth2Token oAuth2Token = tokenGenerator.apply(token);
        return new Token<>(oAuth2Token, metaMap);
    }


    protected static class Token<T extends OAuth2Token> extends OAuth2Authorization.Token<T> {

        protected Token(T token) {
            super(token);
        }

        protected Token(T token, Map<String, Object> metadata) {
            super(token, metadata);
        }
    }
}
