package com.generatera.authorization.server.common.configuration.authorization;

import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
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
/**
 * @author FLJ
 * @date 2023/1/9
 * @time 11:34
 * @Description 默认的LightningAuthorization
 *
 * 主要是 非OAuth2的 普通授权服务器授权成功之后的 authorization 保留的实体
 *
 * 但是是 oauth2 copy ...
 *
 * 这一部分其实算是 JWT兼容的部分
 *
 * 这主要保留 accessToken / Refresh Token ..
 */
public class DefaultLightningAuthorization implements LightningAuthorization, Serializable {

    private String id;
    /**
     * 主体名称
     */
    private String principalName;
    /**
     * tokens
     */
    private Map<Class<? extends LightningToken>, Token<?>> tokens;
    /**
     * 属性
     */
    private Map<String, Object> attributes;

    private LightningUserPrincipal userPrincipal;

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
    @SuppressWarnings("unchecked")
    public <T extends LightningToken> Token<T> getToken(Class<T> tokenType) {
        Assert.notNull(tokenType, "tokenType cannot be null");
        return (Token<T>) this.tokens.get(tokenType);
    }

    @Nullable
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        Assert.hasText(name, "name cannot be empty");
        return (T)this.attributes.get(name);
    }


    public LightningUserPrincipal getPrincipal() {
        return userPrincipal;
    }

    public static Builder from(DefaultLightningAuthorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        return new Builder()
                .id(authorization.getId())
                .principalName(authorization.getPrincipalName())
                .tokens(authorization.tokens)
                .attributes((attrs) -> attrs.putAll(authorization.getAttributes()));
    }



    public static class Builder implements Serializable {
        private String id;
        private String principalName;

        private LightningUserPrincipal princinpal;

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

        public Builder principal(LightningUserPrincipal userPrincipal) {
            this.princinpal = userPrincipal;
            return this;
        }

        public Builder accessToken(LightningAccessToken accessToken) {
            return this.token(accessToken,LightningAccessToken.class);
        }

        public Builder refreshToken(LightningRefreshToken refreshToken) {
            return this.token(refreshToken,LightningRefreshToken.class);
        }

        public <T extends LightningToken> Builder token(T token) {
            return this.token(token,token.getClass());
        }

        public <T extends LightningToken> Builder token(T token,Class<? extends LightningToken> clazz) {
            return this.token(token,clazz, (metadata) -> {
            });
        }

        public <T extends LightningToken> Builder token(T token,Consumer<Map<String,Object>> metadataConsumer) {
            return this.token(token,token.getClass(),metadataConsumer);
        }

        public <T extends LightningToken> Builder token(T token,Class<? extends LightningToken> tokenClass, Consumer<Map<String, Object>> metadataConsumer) {
            Assert.notNull(token, "token cannot be null");
            Map<String, Object> metadata = DefaultLightningAuthorization.Token.defaultMetadata();
            Token<?> existingToken = this.tokens.get(tokenClass);
            if (existingToken != null) {
                metadata.putAll(existingToken.getMetadata());
            }

            metadataConsumer.accept(metadata);
            this.tokens.put(tokenClass, new Token<>(token, metadata));
            return this;
        }

        protected final Builder tokens(Map<Class<? extends LightningToken>, Token<?>> tokens) {
            this.tokens = new HashMap<>(tokens);
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
            authorization.userPrincipal = this.princinpal;

            // 必须存在 ..
            attribute(USER_INFO_ATTRIBUTE_NAME, princinpal);
            return authorization;
        }

    }

    /**
     * token 可能存在元数据信息 ...
     *
     * JWT 兼容
     *
     * 也就是可以修改元数据信息
     * @param <T> LightningToken
     */
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
            return this.getMetadata(CLAIMS_METADATA_NAME);
        }

        @Nullable
        @SuppressWarnings("unchecked")
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
            INVALIDATED_METADATA_NAME = TOKEN_METADATA_NAMESPACE.concat("invalidated");
            CLAIMS_METADATA_NAME = TOKEN_METADATA_NAMESPACE.concat("claims");
        }
    }
}