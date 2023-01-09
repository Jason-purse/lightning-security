package com.generatera.authorization.server.common.configuration.authorization.store;

import com.generatera.authorization.server.common.configuration.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.authorization.server.common.configuration.repository.JpaAuthenticationTokenRepository;
import com.jianyue.lightning.boot.starter.util.SnowflakeIdWorker;
import org.springframework.data.domain.Example;
import org.springframework.util.Assert;

public class JpaAuthenticationTokenService extends AbstractAuthenticationTokenService {
    // TODO: 2023/1/4  分布式环境中,id 冲突
    private final SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker();
    private final JpaAuthenticationTokenRepository repository;

    public JpaAuthenticationTokenService(JpaAuthenticationTokenRepository tokenRepository) {
        Assert.notNull(tokenRepository,"tokenRepository must not be null !!!");
        this.repository = tokenRepository;
    }

    @Override
    protected void doSave(LightningAuthenticationTokenEntity entity) {
        entity.setId(snowflakeIdWorker.nextId());
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
