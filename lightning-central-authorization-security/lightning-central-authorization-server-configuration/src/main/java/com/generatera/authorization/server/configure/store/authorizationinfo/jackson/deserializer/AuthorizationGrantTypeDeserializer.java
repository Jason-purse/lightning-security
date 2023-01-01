package com.generatera.authorization.server.configure.store.authorizationinfo.jackson.deserializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.io.IOException;

public class AuthorizationGrantTypeDeserializer extends StdDeserializer<AuthorizationGrantType> {
    public AuthorizationGrantTypeDeserializer() {
        super(AuthorizationGrantType.class);
    }

    @Override
    public AuthorizationGrantType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode readTree = deserializationContext.readTree(jsonParser);
        String value = readTree.get("value").asText();
        if(StringUtils.isNotBlank(value)) {
            return new AuthorizationGrantType(value);
        }
        return null;
    }
}
