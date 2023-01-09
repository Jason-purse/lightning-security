package com.generatera.authorization.server.common.configuration.authorization;

import com.generatera.security.authorization.server.specification.components.token.LightningToken;
import com.generatera.security.authorization.server.specification.components.token.LightningToken.LightningAccessToken;
import com.generatera.security.authorization.server.specification.components.token.LightningToken.LightningRefreshToken;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

public class DefaultLightningAuthorization implements LightningAuthorization, Serializable {
    public static final String AUTHORIZED_SCOPE_ATTRIBUTE_NAME;
    private String id;
    private String principalName;
    private Map<Class<? extends LightningToken>, Token<?>> tokens;
    private Map<String, Object> attributes;

    protected DefaultLightningAuthorization() {
    }

    public String getId() {
        return this.id;
    }


    public String getPrincipalName() {
        return this.principalName;
    }


    public Token<LightningAccessToken> getAccessToken() {
        return this.getToken(LightningAccessToken.class);
    }

    @Nullable
    public Token<LightningRefreshToken> getRefreshToken() {
        return this.getToken(LightningRefreshToken.class);
    }

    @Nullable
    public <T extends LightningToken> Token<T> getToken(Class<T> tokenType) {
        Assert.notNull(tokenType, "tokenType cannot be null");
        return (Token<T>) this.tokens.get(tokenType);
    }

    @Nullable
    public <T extends LightningToken> Token<T> getToken(String tokenValue) {
        Assert.hasText(tokenValue, "tokenValue cannot be empty");
        Iterator<Token<? extends LightningToken>> var2 = this.tokens.values().iterator();

        Token<T> token;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            token = (Token<T>) var2.next();
        } while(!token.getToken().getTokenValue().equals(tokenValue));

        return token;
    }

    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Nullable
    public <T> T getAttribute(String name) {
        Assert.hasText(name, "name cannot be empty");
        return (T)this.attributes.get(name);
    }




    public static Builder from(DefaultLightningAuthorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        return new Builder()
                .id(authorization.getId())
                .principalName(authorization.getPrincipalName())
                .tokens(authorization.tokens)
                .attributes((attrs) -> attrs.putAll(authorization.getAttributes()));
    }

    static {
        AUTHORIZED_SCOPE_ATTRIBUTE_NAME = DefaultLightningAuthorization.class.getName().concat(".AUTHORIZED_SCOPE");
    }

    public static class Builder implements Serializable {
        private String id;
        private String principalName;
        private Map<Class<? extends LightningToken>, Token<?>> tokens = new HashMap<>();
        private final Map<String, Object> attributes = new HashMap<>();


        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder principalName(String principalName) {
            this.principalName = principalName;
            return this;
        }

        public Builder accessToken(LightningAccessToken accessToken) {
            return this.token(accessToken);
        }

        public Builder refreshToken(LightningRefreshToken refreshToken) {
            return this.token(refreshToken);
        }

        public <T extends LightningToken> Builder token(T token) {
            return this.token(token, (metadata) -> {
            });
        }

        public <T extends LightningToken> Builder token(T token, Consumer<Map<String, Object>> metadataConsumer) {
            Assert.notNull(token, "token cannot be null");
            Map<String, Object> metadata = DefaultLightningAuthorization.Token.defaultMetadata();
            Token<?> existingToken = (Token)this.tokens.get(token.getClass());
            if (existingToken != null) {
                metadata.putAll(existingToken.getMetadata());
            }

            metadataConsumer.accept(metadata);
            Class<? extends LightningToken> tokenClass = token.getClass();
            this.tokens.put(tokenClass, new Token(token, metadata));
            return this;
        }

        protected final Builder tokens(Map<Class<? extends LightningToken>, Token<?>> tokens) {
            this.tokens = new HashMap(tokens);
            return this;
        }

        public Builder attribute(String name, Object value) {
            Assert.hasText(name, "name cannot be empty");
            Assert.notNull(value, "value cannot be null");
            this.attributes.put(name, value);
            return this;
        }

        public Builder attributes(Consumer<Map<String, Object>> attributesConsumer) {
            attributesConsumer.accept(this.attributes);
            return this;
        }

        public DefaultLightningAuthorization build() {
            Assert.hasText(this.principalName, "principalName cannot be empty");
            DefaultLightningAuthorization authorization = new DefaultLightningAuthorization();
            if (!StringUtils.hasText(this.id)) {
                this.id = UUID.randomUUID().toString();
            }

            authorization.id = this.id;
            authorization.principalName = this.principalName;
            authorization.tokens = Collections.unmodifiableMap(this.tokens);
            authorization.attributes = Collections.unmodifiableMap(this.attributes);
            return authorization;
        }

    }

    public static class Token<T extends LightningToken> implements Serializable {
        protected static final String TOKEN_METADATA_NAMESPACE = "metadata.token.";
        public static final String INVALIDATED_METADATA_NAME;
        public static final String CLAIMS_METADATA_NAME;
        private final T token;
        private final Map<String, Object> metadata;

        protected Token(T token) {
            this(token, defaultMetadata());
        }

        protected Token(T token, Map<String, Object> metadata) {
            this.token = token;
            this.metadata = Collections.unmodifiableMap(metadata);
        }

        public T getToken() {
            return this.token;
        }

        public boolean isInvalidated() {
            return Boolean.TRUE.equals(this.getMetadata(INVALIDATED_METADATA_NAME));
        }

        public boolean isExpired() {
            return this.getToken().getExpiresAt() != null && Instant.now().isAfter(this.getToken().getExpiresAt());
        }

        public boolean isBeforeUse() {
            Instant notBefore = null;
            if (!CollectionUtils.isEmpty(this.getClaims())) {
                notBefore = (Instant)this.getClaims().get("nbf");
            }

            return notBefore != null && Instant.now().isBefore(notBefore);
        }

        public boolean isActive() {
            return !this.isInvalidated() && !this.isExpired() && !this.isBeforeUse();
        }

        @Nullable
        public Map<String, Object> getClaims() {
            return (Map)this.getMetadata(CLAIMS_METADATA_NAME);
        }

        @Nullable
        public <V> V getMetadata(String name) {
            Assert.hasText(name, "name cannot be empty");
            return (V)this.metadata.get(name);
        }

        public Map<String, Object> getMetadata() {
            return this.metadata;
        }

        protected static Map<String, Object> defaultMetadata() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put(INVALIDATED_METADATA_NAME, false);
            return metadata;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj != null && this.getClass() == obj.getClass()) {
                Token<?> that = (Token<?>)obj;
                return Objects.equals(this.token, that.token) && Objects.equals(this.metadata, that.metadata);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hash(this.token, this.metadata);
        }

        static {
            INVALIDATED_METADATA_NAME = "metadata.token.".concat("invalidated");
            CLAIMS_METADATA_NAME = "metadata.token.".concat("claims");
        }
    }
}