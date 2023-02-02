package com.generatera.authorization.application.server.config.token;

import com.generatera.security.authorization.server.specification.components.token.format.JwtExtClaimNames;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
/**
 * @author FLJ
 * @date 2023/1/28
 * @time 12:19
 * @Description auth token introspection info
 */
public final class AuthTokenIntrospection implements AuthTokenIntrospectionClaimAccessor, Serializable {

    private final Map<String, Object> claims;

    private List<String> authoritiesName = Arrays.asList(JwtExtClaimNames.SCOPE_CLAIM,JwtExtClaimNames.SCOPE_SHORT_CLAIM);

    public void setAuthoritiesName(List<String> authoritiesName) {
        Assert.notEmpty(authoritiesName,"authoritiesName must not be null !!!");
        this.authoritiesName = authoritiesName;
    }

    private AuthTokenIntrospection(Map<String, Object> claims) {
        this.claims = Collections.unmodifiableMap(new LinkedHashMap<>(claims));
    }

    public Map<String, Object> getClaims() {
        return this.claims;
    }

    public static AuthTokenIntrospection.Builder builder() {
        return builder(false);
    }

    public static AuthTokenIntrospection.Builder builder(boolean active) {
        return new AuthTokenIntrospection.Builder(active);
    }

    public static AuthTokenIntrospection.Builder withClaims(Map<String, Object> claims) {
        Assert.notEmpty(claims, "claims cannot be empty");
        return builder().claims((c) -> {
            c.putAll(claims);
        });
    }


    public static class Builder {

        private final Map<String, Object> claims = new LinkedHashMap();

        private Builder(boolean active) {
            this.active(active);
        }

        public AuthTokenIntrospection.Builder active(boolean active) {
            return this.claim("active", active);
        }

        public AuthTokenIntrospection.Builder scope(String scope) {
            this.addClaimToClaimList("scope", scope);
            return this;
        }

        public AuthTokenIntrospection.Builder scopes(Consumer<List<String>> scopesConsumer) {
            this.acceptClaimValues("scope", scopesConsumer);
            return this;
        }

        public AuthTokenIntrospection.Builder authorities(Consumer<List<String>> authoritiesConsumer) {
            this.acceptClaimValues(JwtExtClaimNames.AUTHORITIES_CLAIM,authoritiesConsumer);
            return this;
        }


        public AuthTokenIntrospection.Builder username(String username) {
            return this.claim("username", username);
        }

        public AuthTokenIntrospection.Builder tokenType(String tokenType) {
            return this.claim("token_type", tokenType);
        }

        public AuthTokenIntrospection.Builder expiresAt(Instant expiresAt) {
            return this.claim("exp", expiresAt);
        }

        public AuthTokenIntrospection.Builder issuedAt(Instant issuedAt) {
            return this.claim("iat", issuedAt);
        }

        public AuthTokenIntrospection.Builder notBefore(Instant notBefore) {
            return this.claim("nbf", notBefore);
        }

        public AuthTokenIntrospection.Builder subject(String subject) {
            return this.claim("sub", subject);
        }

        public AuthTokenIntrospection.Builder audience(String audience) {
            this.addClaimToClaimList("aud", audience);
            return this;
        }

        public AuthTokenIntrospection.Builder audiences(Consumer<List<String>> audiencesConsumer) {
            this.acceptClaimValues("aud", audiencesConsumer);
            return this;
        }

        public AuthTokenIntrospection.Builder issuer(String issuer) {
            return this.claim("iss", issuer);
        }

        public AuthTokenIntrospection.Builder id(String jti) {
            return this.claim("jti", jti);
        }

        public AuthTokenIntrospection.Builder claim(String name, Object value) {
            Assert.hasText(name, "name cannot be empty");
            Assert.notNull(value, "value cannot be null");
            this.claims.put(name, value);
            return this;
        }

        public AuthTokenIntrospection.Builder claims(Consumer<Map<String, Object>> claimsConsumer) {
            claimsConsumer.accept(this.claims);
            return this;
        }

        public AuthTokenIntrospection build() {
            this.validate();
            return new AuthTokenIntrospection(this.claims);
        }

        private void validate() {
            Assert.notNull(this.claims.get("active"), "active cannot be null");
            Assert.isInstanceOf(Boolean.class, this.claims.get("active"), "active must be of type boolean");
            if (this.claims.containsKey("scope")) {
                Assert.isInstanceOf(List.class, this.claims.get("scope"), "scope must be of type List");
            }

            if(this.claims.containsKey(JwtExtClaimNames.AUTHORITIES_CLAIM)) {
                Assert.isInstanceOf(List.class,this.claims.get(JwtExtClaimNames.AUTHORITIES_CLAIM),JwtExtClaimNames.AUTHORITIES_CLAIM + "must be of type List");
            }

            if (this.claims.containsKey("exp")) {
                Assert.isInstanceOf(Instant.class, this.claims.get("exp"), "exp must be of type Instant");
            }

            if (this.claims.containsKey("iat")) {
                Assert.isInstanceOf(Instant.class, this.claims.get("iat"), "iat must be of type Instant");
            }

            if (this.claims.containsKey("nbf")) {
                Assert.isInstanceOf(Instant.class, this.claims.get("nbf"), "nbf must be of type Instant");
            }

            if (this.claims.containsKey("aud")) {
                Assert.isInstanceOf(List.class, this.claims.get("aud"), "aud must be of type List");
            }

            if (this.claims.containsKey("iss")) {
                validateURL(this.claims.get("iss"), "iss must be a valid URL");
            }

        }

        private void addClaimToClaimList(String name, String value) {
            Assert.hasText(name, "name cannot be empty");
            Assert.notNull(value, "value cannot be null");
            this.claims.computeIfAbsent(name, (k) -> {
                return new LinkedList();
            });
            ((List)this.claims.get(name)).add(value);
        }

        private void acceptClaimValues(String name, Consumer<List<String>> valuesConsumer) {
            Assert.hasText(name, "name cannot be empty");
            Assert.notNull(valuesConsumer, "valuesConsumer cannot be null");
            this.claims.computeIfAbsent(name, (k) -> {
                return new LinkedList();
            });
            List<String> values = (List)this.claims.get(name);
            valuesConsumer.accept(values);
        }

        private static void validateURL(Object url, String errorMessage) {
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