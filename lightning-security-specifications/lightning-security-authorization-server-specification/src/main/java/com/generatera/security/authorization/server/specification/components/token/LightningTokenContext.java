package com.generatera.security.authorization.server.specification.components.token;

import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.TokenIssueFormat;
import com.generatera.security.authorization.server.specification.TokenSettingsProperties;
import com.generatera.security.authorization.server.specification.components.provider.ProviderContext;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningTokenValueType;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author FLJ
 * @date 2023/1/16
 * @time 14:25
 * @Description Lightning Token 上下文 ...
 */
public interface LightningTokenContext extends Context {

    @SuppressWarnings("unchecked")
    default Authentication getAuthentication() {
        return this.get(AbstractBuilder.AUTHENTICATION_KEY);
    }

    default LightningUserPrincipal getPrincipal() {
        return this.get(AbstractBuilder.AUTHENTICATION_PRINCIPAL_KEY);
    }

    default ProviderContext getProviderContext() {
        return this.get(ProviderContext.class);
    }

    default TokenSettingsProperties getTokenSettings() {
        return this.get(TokenSettingsProperties.class);
    }

    default LightningTokenValueType getTokenValueType() {
        return this.get(LightningTokenValueType.class);
    }

    default LightningTokenType.LightningTokenValueFormat getTokenValueFormat() {
        return this.get(LightningTokenType.LightningTokenValueFormat.class);
    }

    default TokenIssueFormat getTokenIssueFormat() {
        return this.get(TokenIssueFormat.class);
    }

    default LightningTokenType.LightningAuthenticationTokenType getTokenType() {
        return this.get(LightningTokenType.LightningAuthenticationTokenType.class);
    }


    Map<Object,Object> getContexts();
    /**
     * 抽象构建器
     */
    abstract class AbstractBuilder<T extends LightningTokenContext, B extends AbstractBuilder<T, B>> {
        private static final String AUTHENTICATION_KEY = Authentication.class.getSimpleName();
        private static final String AUTHENTICATION_PRINCIPAL_KEY = AUTHENTICATION_KEY.concat(Principal.class.getSimpleName());
        /**
         * 各种上下文集合 ...
         */
        private final Map<Object, Object> context = new HashMap<>();

        public AbstractBuilder() {
        }

        public B authentication(Authentication authentication) {
            this.put(AUTHENTICATION_KEY, authentication);
            // 填充 ..
            return this.principal(((LightningUserPrincipal) authentication.getPrincipal()));
        }

        public B principal(LightningUserPrincipal userPrincipal) {
            return this.put(AUTHENTICATION_PRINCIPAL_KEY, userPrincipal);
        }

        public B providerContext(ProviderContext providerContext) {
            return this.put(ProviderContext.class, providerContext);
        }

        public B tokenValueType(LightningTokenValueType tokenValueType) {
            return this.put(LightningTokenValueType.class, tokenValueType);
        }

        public B tokenValueFormat(LightningTokenType.LightningTokenValueFormat tokenValueFormat) {
            return this.put(LightningTokenType.LightningTokenValueFormat.class, tokenValueFormat);
        }

        public B tokenIssueFormat(TokenIssueFormat tokenIssueFormat) {
            return this.put(TokenIssueFormat.class, tokenIssueFormat);
        }

        public B tokenType(LightningTokenType.LightningAuthenticationTokenType tokenType) {
            return this.put(LightningTokenType.LightningAuthenticationTokenType.class, tokenType);
        }

        public B tokenSettings(TokenSettingsProperties tokenSettings) {
            return this.put(TokenSettingsProperties.class, tokenSettings);
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

        @SuppressWarnings("unchecked")
        protected <V> V get(Object key) {
            return (V) this.context.get(key);
        }

        protected Map<Object, Object> getContext() {
            return this.context;
        }

        @SuppressWarnings("unchecked")
        protected final B getThis() {
            return (B) this;
        }

        public abstract T build();
    }
}