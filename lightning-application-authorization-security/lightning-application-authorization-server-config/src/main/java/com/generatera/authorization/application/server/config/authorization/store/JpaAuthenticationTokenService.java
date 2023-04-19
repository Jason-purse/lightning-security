package com.generatera.authorization.application.server.config.authorization.store;

import com.generatera.authorization.application.server.config.authorization.store.dao.JpaAuthenticationTokenRepository;
import com.generatera.authorization.application.server.config.model.entity.ForDBAuthenticationTokenEntity;
import com.generatera.security.authorization.server.specification.LightningUserPrincipalConverter;
import com.jianyue.lightning.boot.starter.util.OptionalFlux;
import com.jianyue.lightning.boot.starter.util.StreamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import java.time.Instant;
import java.util.List;

public class JpaAuthenticationTokenService extends ForDBAuthenticationTokenService implements LazyAuthenticationTokenService.TokenClearer {

    @Autowired
    private JpaAuthenticationTokenRepository repository;

    public JpaAuthenticationTokenService(LightningUserPrincipalConverter userPrincipalConverter){
        super(userPrincipalConverter);
    }



    @Override
    protected void doSave0(ForDBAuthenticationTokenEntity entity) {
        repository.save(entity);
    }

    @Override
    protected void doRemove0(ForDBAuthenticationTokenEntity tokenEntity) {
        repository.delete((tokenEntity));
    }


    @Override
    protected ForDBAuthenticationTokenEntity doFindById0(ForDBAuthenticationTokenEntity tokenEntity) {
        return repository.findOne(Example.of(tokenEntity)).orElse(null);
    }

    @Override
    protected ForDBAuthenticationTokenEntity doFindAccessOrRefreshTokenByToken0(String token) {
        return repository.findFirstByAccessTokenValueIsOrRefreshTokenValueIs(token,token);
    }

    @Override
    protected ForDBAuthenticationTokenEntity doFindByToken0(ForDBAuthenticationTokenEntity entity) {
        return repository.findOne(Example.of(entity)).orElse(null);
    }

    @Override
    public void clearInvalidToken() {
        // 删除 ...
        List<ForDBAuthenticationTokenEntity> values = repository.findAllByAccessExpiredAtGreaterThanEqual(Instant.now().toEpochMilli());
        OptionalFlux.list(values)
                .map(StreamUtil.listMap(ForDBAuthenticationTokenEntity::getId))
                .consume(repository::deleteAllById);
    }
}
