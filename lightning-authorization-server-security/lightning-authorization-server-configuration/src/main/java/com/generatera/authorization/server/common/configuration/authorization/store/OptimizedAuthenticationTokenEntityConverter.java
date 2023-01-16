package com.generatera.authorization.server.common.configuration.authorization.store;

import com.generatera.authorization.server.common.configuration.authorization.DefaultLightningAuthorization;
import com.generatera.authorization.server.common.configuration.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import org.springframework.util.Assert;

public class OptimizedAuthenticationTokenEntityConverter extends AuthenticationTokenEntityConverter {

    public  LightningUserPrincipalConverter userPrincipalConverter;

    public OptimizedAuthenticationTokenEntityConverter(LightningUserPrincipalConverter userPrincipalConverter) {
        Assert.notNull(userPrincipalConverter,"userPrincipalConverter must not be null !!!");
        this.userPrincipalConverter = userPrincipalConverter;
    }

    public void setUserPrincipalConverter(LightningUserPrincipalConverter userPrincipalConverter) {
        Assert.notNull(userPrincipalConverter,"userPrincipalConverter must not be null !!!");
        this.userPrincipalConverter = userPrincipalConverter;
    }

    @Override
    public LightningAuthenticationTokenEntity convert(DefaultLightningAuthorization source) {
        LightningAuthenticationTokenEntity entity = super.convert(source);
        LightningUserPrincipal principal = (LightningUserPrincipal) entity.getUserPrincipal();
        assert principal != null;
        Object userPrincipal = this.userPrincipalConverter.serialize(principal);
        entity.setUserPrincipal(userPrincipal);
        return entity;
    }
}
