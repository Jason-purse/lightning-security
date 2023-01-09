package com.generatera.authorization.server.common.configuration.authorization.store;

import com.generatera.authorization.server.common.configuration.model.entity.LightningAuthenticationTokenEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class MongoAuthenticationTokenService extends AbstractAuthenticationTokenService {

    @Autowired
    private MongoTemplate mongoTemplate;


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
