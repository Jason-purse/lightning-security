package com.generatera.authorization.server.common.configuration.token;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public interface LightningTokenContext extends Context {

    default <T extends Authentication> T getPrincipal() {
        return (T)this.get(LightningTokenContext.AbstractBuilder.PRINCIPAL_AUTHENTICATION_KEY);
    }

    default ProviderContext getProviderContext() {
        return (ProviderContext)this.get(ProviderContext.class);
    }

    default Set<String> getAuthorizedScopes() {
        return this.hasKey(OAuth2Authorization.AUTHORIZED_SCOPE_ATTRIBUTE_NAME) ? (Set)this.get(OAuth2Authorization.AUTHORIZED_SCOPE_ATTRIBUTE_NAME) : Collections.emptySet();
    }

    default OAuth2TokenType getTokenType() {
        return (OAuth2TokenType)this.get(OAuth2TokenType.class);
    }

    default AuthorizationGrantType getAuthorizationGrantType() {
        return (AuthorizationGrantType)this.get(AuthorizationGrantType.class);
    }

    default <T extends Authentication> T getAuthorizationGrant() {
        return (T)this.get(LightningTokenContext.AbstractBuilder.AUTHORIZATION_GRANT_AUTHENTICATION_KEY);
    }

    public abstract static class AbstractBuilder<T extends LightningTokenContext, B extends LightningTokenContext.AbstractBuilder<T, B>> {
        private static final String PRINCIPAL_AUTHENTICATION_KEY = Authentication.class.getName().concat(".PRINCIPAL");
        private static final String AUTHORIZATION_GRANT_AUTHENTICATION_KEY = Authentication.class.getName().concat(".AUTHORIZATION_GRANT");
        private final Map<Object, Object> context = new HashMap<>();

        public AbstractBuilder() {
        }

        public B registeredClient(RegisteredClient registeredClient) {
            return this.put(RegisteredClient.class, registeredClient);
        }

        public B principal(Authentication principal) {
            return this.put(PRINCIPAL_AUTHENTICATION_KEY, principal);
        }

        public B providerContext(ProviderContext providerContext) {
            return this.put(ProviderContext.class, providerContext);
        }

        public B authorizedScopes(Set<String> authorizedScopes) {
            return this.put(OAuth2Authorization.AUTHORIZED_SCOPE_ATTRIBUTE_NAME, authorizedScopes);
        }

        public B tokenType(LightningToken.TokenType tokenType) {
            return this.put(LightningToken.TokenType.class, tokenType);
        }


        public B put(Object key, Object value) {
            Assert.notNull(key, "key cannot be null");
            Assert.notNull(value, "value cannot be null");
            this.context.put(key, value);
            return this.getThis();
        }

        public B context(Consumer<Map<Object, Object>> contextConsumer) {
            contextConsumer.accept(this.context);
            return this.getThis();
        }

        protected <V> V get(Object key) {
            return (V)this.context.get(key);
        }

        protected Map<Object, Object> getContext() {
            return this.context;
        }

        protected final B getThis() {
            return (B)this;
        }

        public abstract T build();
    }
}