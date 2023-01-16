package com.generatera.authorization.server.common.configuration.authorization.store;

import com.generatera.authorization.server.common.configuration.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.authorization.server.common.configuration.repository.JpaAuthenticationTokenRepository;
import com.generatera.security.authorization.server.specification.components.token.format.plain.UuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

public class JpaAuthenticationTokenService extends AbstractAuthenticationTokenService {

    @Autowired
    private  JpaAuthenticationTokenRepository repository;

    public JpaAuthenticationTokenService(LightningUserPrincipalConverter userPrincipalConverter){
        super(userPrincipalConverter);
    }



    @Override
    protected void doSave(LightningAuthenticationTokenEntity entity) {
        entity.setId(UuidUtil.nextId());
        repository.save(entity);
    }

    @Override
    protected void doRemove(LightningAuthenticationTokenEntity entity) {
        repository.delete(entity);
    }

    @Override
    public LightningAuthenticationTokenEntity doFindById(LightningAuthenticationTokenEntity entity) {
        return repository.findOne(Example.of(entity)).orElse(null);
    }

    @Override
    protected LightningAuthenticationTokenEntity doFindAccessOrRefreshTokenByToken(String token) {
        return repository.findFirstByAccessTokenValueIsOrRefreshTokenValueIs(token,token);
    }

    @Override
    public LightningAuthenticationTokenEntity doFindByToken(LightningAuthenticationTokenEntity entity) {
        return repository.findOne(Example.of(entity)).orElse(null);
    }
}
