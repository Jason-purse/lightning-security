package com.generatera.authorization.application.server.config.specification;

import org.springframework.security.oauth2.core.Version;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

public abstract class AbstractOAuth2AuthorizationServerMetadata implements OAuth2AuthorizationServerMetadataClaimAccessor, Serializable {
    private static final long serialVersionUID;
    private final Map<String, Object> claims;

    protected AbstractOAuth2AuthorizationServerMetadata(Map<String, Object> claims) {
        Assert.notEmpty(claims, "claims cannot be empty");
        this.claims = Collections.unmodifiableMap(new LinkedHashMap(claims));
    }

    public Map<String, Object> getClaims() {
        return this.claims;
    }

    static {
        serialVersionUID = Version.SERIAL_VERSION_UID;
    }

    protected abstract static class AbstractBuilder<T extends AbstractOAuth2AuthorizationServerMetadata, B extends AbstractOAuth2AuthorizationServerMetadata.AbstractBuilder<T, B>> {
        private final Map<String, Object> claims = new LinkedHashMap();

        protected AbstractBuilder() {
        }

        protected Map<String, Object> getClaims() {
            return this.claims;
        }

        protected final B getThis() {
            return (B)this;
        }

        public B issuer(String issuer) {
            return this.claim("issuer", issuer);
        }

        public B authorizationEndpoint(String authorizationEndpoint) {
            return this.claim("authorization_endpoint", authorizationEndpoint);
        }

        public B tokenEndpoint(String tokenEndpoint) {
            return this.claim("token_endpoint", tokenEndpoint);
        }

        public B tokenEndpointAuthenticationMethod(String authenticationMethod) {
            this.addClaimToClaimList("token_endpoint_auth_methods_supported", authenticationMethod);
            return this.getThis();
        }

        public B tokenEndpointAuthenticationMethods(Consumer<List<String>> authenticationMethodsConsumer) {
            this.acceptClaimValues("token_endpoint_auth_methods_supported", authenticationMethodsConsumer);
            return this.getThis();
        }

        public B jwkSetUrl(String jwkSetUrl) {
            return this.claim("jwks_uri", jwkSetUrl);
        }

        public B scope(String scope) {
            this.addClaimToClaimList("scopes_supported", scope);
            return this.getThis();
        }

        public B scopes(Consumer<List<String>> scopesConsumer) {
            this.acceptClaimValues("scopes_supported", scopesConsumer);
            return this.getThis();
        }

        public B responseType(String responseType) {
            this.addClaimToClaimList("response_types_supported", responseType);
            return this.getThis();
        }

        public B responseTypes(Consumer<List<String>> responseTypesConsumer) {
            this.acceptClaimValues("response_types_supported", responseTypesConsumer);
            return this.getThis();
        }

        public B grantType(String grantType) {
            this.addClaimToClaimList("grant_types_supported", grantType);
            return this.getThis();
        }

        public B grantTypes(Consumer<List<String>> grantTypesConsumer) {
            this.acceptClaimValues("grant_types_supported", grantTypesConsumer);
            return this.getThis();
        }

        public B tokenRevocationEndpoint(String tokenRevocationEndpoint) {
            return this.claim("revocation_endpoint", tokenRevocationEndpoint);
        }

        public B tokenRevocationEndpointAuthenticationMethod(String authenticationMethod) {
            this.addClaimToClaimList("revocation_endpoint_auth_methods_supported", authenticationMethod);
            return this.getThis();
        }

        public B tokenRevocationEndpointAuthenticationMethods(Consumer<List<String>> authenticationMethodsConsumer) {
            this.acceptClaimValues("revocation_endpoint_auth_methods_supported", authenticationMethodsConsumer);
            return this.getThis();
        }

        public B tokenIntrospectionEndpoint(String tokenIntrospectionEndpoint) {
            return this.claim("introspection_endpoint", tokenIntrospectionEndpoint);
        }

        public B tokenIntrospectionEndpointAuthenticationMethod(String authenticationMethod) {
            this.addClaimToClaimList("introspection_endpoint_auth_methods_supported", authenticationMethod);
            return this.getThis();
        }

        public B tokenIntrospectionEndpointAuthenticationMethods(Consumer<List<String>> authenticationMethodsConsumer) {
            this.acceptClaimValues("introspection_endpoint_auth_methods_supported", authenticationMethodsConsumer);
            return this.getThis();
        }

        public B codeChallengeMethod(String codeChallengeMethod) {
            this.addClaimToClaimList("code_challenge_methods_supported", codeChallengeMethod);
            return this.getThis();
        }

        public B codeChallengeMethods(Consumer<List<String>> codeChallengeMethodsConsumer) {
            this.acceptClaimValues("code_challenge_methods_supported", codeChallengeMethodsConsumer);
            return this.getThis();
        }

        public B claim(String name, Object value) {
            Assert.hasText(name, "name cannot be empty");
            Assert.notNull(value, "value cannot be null");
            this.claims.put(name, value);
            return this.getThis();
        }

        public B claims(Consumer<Map<String, Object>> claimsConsumer) {
            claimsConsumer.accept(this.claims);
            return this.getThis();
        }

        public abstract T build();

        protected void validate() {
            Assert.notNull(this.getClaims().get("issuer"), "issuer cannot be null");
            validateURL(this.getClaims().get("issuer"), "issuer must be a valid URL");
            Assert.notNull(this.getClaims().get("authorization_endpoint"), "authorizationEndpoint cannot be null");
            validateURL(this.getClaims().get("authorization_endpoint"), "authorizationEndpoint must be a valid URL");
            Assert.notNull(this.getClaims().get("token_endpoint"), "tokenEndpoint cannot be null");
            validateURL(this.getClaims().get("token_endpoint"), "tokenEndpoint must be a valid URL");
            if (this.getClaims().get("token_endpoint_auth_methods_supported") != null) {
                Assert.isInstanceOf(List.class, this.getClaims().get("token_endpoint_auth_methods_supported"), "tokenEndpointAuthenticationMethods must be of type List");
                Assert.notEmpty((List)this.getClaims().get("token_endpoint_auth_methods_supported"), "tokenEndpointAuthenticationMethods cannot be empty");
            }

            if (this.getClaims().get("jwks_uri") != null) {
                validateURL(this.getClaims().get("jwks_uri"), "jwksUri must be a valid URL");
            }

            if (this.getClaims().get("scopes_supported") != null) {
                Assert.isInstanceOf(List.class, this.getClaims().get("scopes_supported"), "scopes must be of type List");
                Assert.notEmpty((List)this.getClaims().get("scopes_supported"), "scopes cannot be empty");
            }

            Assert.notNull(this.getClaims().get("response_types_supported"), "responseTypes cannot be null");
            Assert.isInstanceOf(List.class, this.getClaims().get("response_types_supported"), "responseTypes must be of type List");
            Assert.notEmpty((List)this.getClaims().get("response_types_supported"), "responseTypes cannot be empty");
            if (this.getClaims().get("grant_types_supported") != null) {
                Assert.isInstanceOf(List.class, this.getClaims().get("grant_types_supported"), "grantTypes must be of type List");
                Assert.notEmpty((List)this.getClaims().get("grant_types_supported"), "grantTypes cannot be empty");
            }

            if (this.getClaims().get("revocation_endpoint") != null) {
                validateURL(this.getClaims().get("revocation_endpoint"), "tokenRevocationEndpoint must be a valid URL");
            }

            if (this.getClaims().get("revocation_endpoint_auth_methods_supported") != null) {
                Assert.isInstanceOf(List.class, this.getClaims().get("revocation_endpoint_auth_methods_supported"), "tokenRevocationEndpointAuthenticationMethods must be of type List");
                Assert.notEmpty((List)this.getClaims().get("revocation_endpoint_auth_methods_supported"), "tokenRevocationEndpointAuthenticationMethods cannot be empty");
            }

            if (this.getClaims().get("introspection_endpoint") != null) {
                validateURL(this.getClaims().get("introspection_endpoint"), "tokenIntrospectionEndpoint must be a valid URL");
            }

            if (this.getClaims().get("introspection_endpoint_auth_methods_supported") != null) {
                Assert.isInstanceOf(List.class, this.getClaims().get("introspection_endpoint_auth_methods_supported"), "tokenIntrospectionEndpointAuthenticationMethods must be of type List");
                Assert.notEmpty((List)this.getClaims().get("introspection_endpoint_auth_methods_supported"), "tokenIntrospectionEndpointAuthenticationMethods cannot be empty");
            }

            if (this.getClaims().get("code_challenge_methods_supported") != null) {
                Assert.isInstanceOf(List.class, this.getClaims().get("code_challenge_methods_supported"), "codeChallengeMethods must be of type List");
                Assert.notEmpty((List)this.getClaims().get("code_challenge_methods_supported"), "codeChallengeMethods cannot be empty");
            }

        }

        private void addClaimToClaimList(String name, String value) {
            Assert.hasText(name, "name cannot be empty");
            Assert.notNull(value, "value cannot be null");
            this.getClaims().computeIfAbsent(name, (k) -> {
                return new LinkedList();
            });
            ((List)this.getClaims().get(name)).add(value);
        }

        private void acceptClaimValues(String name, Consumer<List<String>> valuesConsumer) {
            Assert.hasText(name, "name cannot be empty");
            Assert.notNull(valuesConsumer, "valuesConsumer cannot be null");
            this.getClaims().computeIfAbsent(name, (k) -> {
                return new LinkedList();
            });
            List<String> values = (List)this.getClaims().get(name);
            valuesConsumer.accept(values);
        }

        protected static void validateURL(Object url, String errorMessage) {
            if (!URL.class.isAssignableFrom(url.getClass())) {
                try {
                    (new URI(url.toString())).toURL();
                } catch (Exception var3) {
                    throw new IllegalArgumentException(errorMessage, var3);
                }
            }
        }
    }
}
