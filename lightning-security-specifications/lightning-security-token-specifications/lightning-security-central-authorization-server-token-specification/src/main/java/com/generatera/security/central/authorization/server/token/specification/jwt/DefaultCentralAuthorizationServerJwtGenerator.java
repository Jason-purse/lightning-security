package com.generatera.security.central.authorization.server.token.specification.jwt;

import com.generatera.security.server.token.specification.LightningAuthorizationServerTokenSecurityContext;
import com.generatera.security.server.token.specification.format.jwt.LightningJwt;
import com.generatera.security.server.token.specification.format.jwt.LightningJwtGenerator;
import org.springframework.util.Assert;

public class DefaultCentralAuthorizationServerJwtGenerator implements LightningCentralAuthorizationServerJwtGenerator {
    public final LightningJwtGenerator jwtGenerator;
    public DefaultCentralAuthorizationServerJwtGenerator(LightningJwtGenerator jwtGenerator) {
        Assert.notNull(jwtGenerator,"jwtGenerator must not be null !!!");
        this.jwtGenerator = jwtGenerator;
    }
    @Override
    public LightningJwt generate(LightningAuthorizationServerTokenSecurityContext securityContext) {
        return jwtGenerator.generate(securityContext);
    }
}
