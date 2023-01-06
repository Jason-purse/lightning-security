package com.generatera.security.server.token.specification.format.jwt;

import com.generatera.security.server.token.specification.LightningAuthorizationServerTokenSecurityContext;

public interface LightningJwtGenerator{

    LightningJwt generate(LightningAuthorizationServerTokenSecurityContext securityContext);
}
