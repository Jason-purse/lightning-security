package com.generatera.security.authorization.server.specification.components.token;

import com.generatera.security.authorization.server.specification.components.provider.ProviderContext;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningTokenValueType;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public interface LightningTokenContext extends Context {

    default <T extends Authentication> T getPrincipal() {
        return (T)this.get(AbstractBuilder.PRINCIPAL_AUTHENTICATION_KEY);
    }

    default ProviderContext getProviderContext() {
        return (ProviderContext)this.get(ProviderContext.class);
    }

    default LightningTokenValueType getTokenValueType() {
        return this.get(LightningTokenValueType.class);
    }

    default LightningTokenType.LightningAuthenticationTokenType getTokenType() {
        return this.get(LightningTokenType.LightningAuthenticationTokenType.class);
    }

    default <T extends Authentication> T getAuthorizationGrant() {
        return (T)this.get(AbstractBuilder.AUTHORIZATION_GRANT_AUTHENTICATION_KEY);
    }

    public abstract static class AbstractBuilder<T extends LightningTokenContext, B extends AbstractBuilder<T, B>> {
        private static final String PRINCIPAL_AUTHENTICATION_KEY = Authentication.class.getName().concat(".PRINCIPAL");
        private static final String AUTHORIZATION_GRANT_AUTHENTICATION_KEY = Authentication.class.getName().concat(".AUTHORIZATION_GRANT");
        private final Map<Object, Object> context = new HashMap<>();

        public AbstractBuilder() {
        }

        public B principal(Authentication principal) {
            return this.put(PRINCIPAL_AUTHENTICATION_KEY, principal);
        }

        public B providerContext(ProviderContext providerContext) {
            return this.put(ProviderContext.class, providerContext);
        }


        public B tokenValueType(LightningTokenValueType tokenValueType) {
            return this.put(LightningTokenValueType.class, tokenValueType);
        }

        public B tokenType(LightningTokenType.LightningAuthenticationTokenType tokenType) {
            return this.put(LightningTokenType.LightningAuthenticationTokenType.class,tokenType);
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