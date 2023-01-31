package com.generatera.authorization.application.server.config.authorization.store;

import com.generatera.authorization.application.server.config.authorization.store.dao.JpaAuthenticationTokenRepository;
import com.generatera.authorization.application.server.config.model.entity.ForDBAuthenticationTokenEntity;
import com.generatera.authorization.application.server.config.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.security.authorization.server.specification.LightningUserPrincipalConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

public class JpaAuthenticationTokenService extends ForDBAuthenticationTokenService {

    @Autowired
    private JpaAuthenticationTokenRepository repository;

    public JpaAuthenticationTokenService(LightningUserPrincipalConverter userPrincipalConverter){
        super(userPrincipalConverter);
    }



    @Override
    protected void doSave(LightningAuthenticationTokenEntity entity) {
        repository.save(((ForDBAuthenticationTokenEntity) entity));
    }



    @Override
    protected void doRemove(LightningAuthenticationTokenEntity entity) {

    }

    @Override
    protected void doRemove0(ForDBAuthenticationTokenEntity tokenEntity) {
        repository.delete((tokenEntity));
    }


    @Override
    protected LightningAuthenticationTokenEntity doFindById0(ForDBAuthenticationTokenEntity tokenEntity) {
        return repository.findOne(Example.of(tokenEntity)).orElse(null);
    }

    @Override
    protected LightningAuthenticationTokenEntity doFindAccessOrRefreshTokenByToken(String token) {
        return repository.findFirstByAccessTokenValueIsOrRefreshTokenValueIs(token,token);
    }

    @Override
    protected LightningAuthenticationTokenEntity doFindByToken0(ForDBAuthenticationTokenEntity entity) {
        return repository.findOne(Example.of(entity)).orElse(null);
    }
}
