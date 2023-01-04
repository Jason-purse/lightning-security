package com.generatera.authorization.application.server.config.specification.authorization.store;

import com.generatera.authorization.application.server.config.model.entity.LightningAuthenticationTokenEntity;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;

public class MongoAuthenticationTokenService extends AbstractAuthenticationTokenService {

    private final MongoTemplate mongoTemplate;

    public MongoAuthenticationTokenService(MongoTemplate mongoTemplate) {
        Assert.notNull(mongoTemplate, "mongoTemplate must not be null !!!");
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    protected void doSave(LightningAuthenticationTokenEntity entity) {
        mongoTemplate.save(entity);
    }

    @Override
    protected void doRemove(LightningAuthenticationTokenEntity entity) {
        mongoTemplate.remove(entity);
    }

    @Override
    public LightningAuthenticationTokenEntity doFindById(LightningAuthenticationTokenEntity entity) {
        return mongoTemplate.findOne(
                Query.query(Criteria.where("_id").is(entity.getId())),
                LightningAuthenticationTokenEntity.class
        );
    }

    @Override
    protected LightningAuthenticationTokenEntity doFindAccessOrRefreshTokenByToken(String token) {
        return mongoTemplate.findOne(
                Query.query(
                        Criteria.where("access_token_value").is(token)
                                .orOperator(
                                        Criteria.where("refresh_token_value")
                                                .is(token)
                                )
                ),
                LightningAuthenticationTokenEntity.class
        );
    }

    @Override
    public LightningAuthenticationTokenEntity doFindByToken(LightningAuthenticationTokenEntity entity) {
        return null;
    }
}
