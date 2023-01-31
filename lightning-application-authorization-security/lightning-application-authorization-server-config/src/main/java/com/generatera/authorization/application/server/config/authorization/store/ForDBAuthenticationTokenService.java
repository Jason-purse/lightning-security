package com.generatera.authorization.application.server.config.authorization.store;

import com.generatera.authorization.application.server.config.model.entity.ForDBAuthenticationTokenEntity;
import com.generatera.authorization.application.server.config.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.security.authorization.server.specification.LightningUserPrincipalConverter;
import com.jianyue.lightning.boot.starter.util.BeanUtils;
import org.jetbrains.annotations.NotNull;

public abstract class ForDBAuthenticationTokenService extends AbstractAuthenticationTokenService {

    public ForDBAuthenticationTokenService(LightningUserPrincipalConverter userPrincipalConverter) {
        super(userPrincipalConverter);
    }

    @Override
    protected void doRemove(LightningAuthenticationTokenEntity entity) {
        ForDBAuthenticationTokenEntity tokenEntity = convertEntity(entity);
        doRemove0(tokenEntity);
    }

    @NotNull
    protected ForDBAuthenticationTokenEntity convertEntity(LightningAuthenticationTokenEntity entity) {
        ForDBAuthenticationTokenEntity tokenEntity = BeanUtils.transformFrom(entity, ForDBAuthenticationTokenEntity.class);
        assert tokenEntity != null;
        return tokenEntity;
    }

    protected abstract void doRemove0(ForDBAuthenticationTokenEntity tokenEntity);

    @Override
    public LightningAuthenticationTokenEntity doFindById(LightningAuthenticationTokenEntity entity) {
        return doFindById0(convertEntity(entity));
    }

    protected abstract LightningAuthenticationTokenEntity doFindById0(ForDBAuthenticationTokenEntity tokenEntity);



    @Override
    public LightningAuthenticationTokenEntity doFindByToken(LightningAuthenticationTokenEntity entity) {
        return doFindByToken0(convertEntity(entity));
    }

    protected abstract LightningAuthenticationTokenEntity doFindByToken0(ForDBAuthenticationTokenEntity entity);
}
