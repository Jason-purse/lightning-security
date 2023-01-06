package com.generatera.authorization.application.server.config.specification;


import com.generatera.security.server.token.specification.format.jwt.ClaimAccessor;

import java.net.URL;
import java.util.List;

public interface BasedOAuth2AuthorizationServerMetadataClaimAccessor extends ClaimAccessor {
    default URL getIssuer() {
        return this.getClaimAsURL("issuer");
    }

    default URL getAuthorizationEndpoint() {
        return this.getClaimAsURL("authorization_endpoint");
    }

    default URL getTokenEndpoint() {
        return this.getClaimAsURL("token_endpoint");
    }

    default List<String> getTokenEndpointAuthenticationMethods() {
        return this.getClaimAsStringList("token_endpoint_auth_methods_supported");
    }

    default URL getJwkSetUrl() {
        return this.getClaimAsURL("jwks_uri");
    }

    default List<String> getScopes() {
        return this.getClaimAsStringList("scopes_supported");
    }

    default List<String> getResponseTypes() {
        return this.getClaimAsStringList("response_types_supported");
    }

    default List<String> getGrantTypes() {
        return this.getClaimAsStringList("grant_types_supported");
    }

    default URL getTokenRevocationEndpoint() {
        return this.getClaimAsURL("revocation_endpoint");
    }

    default List<String> getTokenRevocationEndpointAuthenticationMethods() {
        return this.getClaimAsStringList("revocation_endpoint_auth_methods_supported");
    }

    default URL getTokenIntrospectionEndpoint() {
        return this.getClaimAsURL("introspection_endpoint");
    }

    default List<String> getTokenIntrospectionEndpointAuthenticationMethods() {
        return this.getClaimAsStringList("introspection_endpoint_auth_methods_supported");
    }

    default List<String> getCodeChallengeMethods() {
        return this.getClaimAsStringList("code_challenge_methods_supported");
    }
}