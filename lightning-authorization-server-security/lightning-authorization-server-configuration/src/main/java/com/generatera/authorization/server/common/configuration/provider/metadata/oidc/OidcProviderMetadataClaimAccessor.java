package com.generatera.authorization.server.common.configuration.provider.metadata.oidc;

import com.generatera.authorization.server.common.configuration.provider.metadata.AuthorizationServerMetadataClaimAccessor;

import java.net.URL;
import java.util.List;

public interface OidcProviderMetadataClaimAccessor extends AuthorizationServerMetadataClaimAccessor {
    default List<String> getSubjectTypes() {
        return this.getClaimAsStringList("subject_types_supported");
    }

    default List<String> getIdTokenSigningAlgorithms() {
        return this.getClaimAsStringList("id_token_signing_alg_values_supported");
    }

    default URL getUserInfoEndpoint() {
        return this.getClaimAsURL("userinfo_endpoint");
    }
}