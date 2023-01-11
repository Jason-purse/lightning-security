package com.generatera.authorization.server.common.configuration.provider.metadata.oidc;

import com.generatera.authorization.server.common.configuration.provider.metadata.AbstractAuthorizationServerMetadata;
import org.springframework.util.Assert;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
/**
 * @author FLJ
 * @date 2023/1/11
 * @time 15:30
 * @Description 基础的OidcProvider  丢弃了oauth2相关一部分的东西
 */
public final class OidcProviderConfiguration extends AbstractAuthorizationServerMetadata implements OidcProviderMetadataClaimAccessor {
    private OidcProviderConfiguration(Map<String, Object> claims) {
        super(claims);
    }

    public static OidcProviderConfiguration.Builder builder() {
        return new OidcProviderConfiguration.Builder();
    }

    public static OidcProviderConfiguration.Builder withClaims(Map<String, Object> claims) {
        Assert.notEmpty(claims, "claims cannot be empty");
        return (new Builder()).claims((c) -> {
            c.putAll(claims);
        });
    }

    public static class Builder extends AbstractBuilder<OidcProviderConfiguration, OidcProviderConfiguration.Builder> {
        private Builder() {
        }

        public OidcProviderConfiguration.Builder subjectType(String subjectType) {
            this.addClaimToClaimList("subject_types_supported", subjectType);
            return this;
        }

        public OidcProviderConfiguration.Builder subjectTypes(Consumer<List<String>> subjectTypesConsumer) {
            this.acceptClaimValues("subject_types_supported", subjectTypesConsumer);
            return this;
        }

        public OidcProviderConfiguration.Builder idTokenSigningAlgorithm(String signingAlgorithm) {
            this.addClaimToClaimList("id_token_signing_alg_values_supported", signingAlgorithm);
            return this;
        }

        public OidcProviderConfiguration.Builder idTokenSigningAlgorithms(Consumer<List<String>> signingAlgorithmsConsumer) {
            this.acceptClaimValues("id_token_signing_alg_values_supported", signingAlgorithmsConsumer);
            return this;
        }


        public OidcProviderConfiguration build() {
            this.validate();
            return new OidcProviderConfiguration(this.getClaims());
        }

        protected void validate() {
            super.validate();
            Assert.notNull(this.getClaims().get("jwks_uri"), "jwksUri cannot be null");
            Assert.notNull(this.getClaims().get("subject_types_supported"), "subjectTypes cannot be null");
            Assert.isInstanceOf(List.class, this.getClaims().get("subject_types_supported"), "subjectTypes must be of type List");
            Assert.notEmpty((List)this.getClaims().get("subject_types_supported"), "subjectTypes cannot be empty");
            Assert.notNull(this.getClaims().get("id_token_signing_alg_values_supported"), "idTokenSigningAlgorithms cannot be null");
            Assert.isInstanceOf(List.class, this.getClaims().get("id_token_signing_alg_values_supported"), "idTokenSigningAlgorithms must be of type List");
            Assert.notEmpty((List)this.getClaims().get("id_token_signing_alg_values_supported"), "idTokenSigningAlgorithms cannot be empty");

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
    }
}