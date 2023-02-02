package com.generatera.authorization.application.server.oauth2.login.config.authorization.request;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.AuthorizationRequestEntity;
import com.jianyue.lightning.boot.starter.util.lambda.LambdaUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;

public class MongoAuthorizationRequestRepository extends AbstractLightningAuthorizationRequestRepository {

    private final MongoTemplate mongoTemplate;

    public MongoAuthorizationRequestRepository(MongoTemplate mongoTemplate) {
        Assert.notNull(mongoTemplate, "mongoTemplate must not be null !!!");
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    protected AuthorizationRequestEntity getInternalAuthorizationRequestEntity(String stateParameter) {
        return mongoTemplate.findOne(
                Query.query(
                        Criteria.where(
                                        LambdaUtils.getPropertyNameForLambda(AuthorizationRequestEntity::getState))
                                .is(stateParameter)
                ), AuthorizationRequestEntity.class);
    }

    @Override
    protected void saveAuthorizationRequestEntity(AuthorizationRequestEntity entity) {
        mongoTemplate.save(entity);
    }

    @Override
    protected AuthorizationRequestEntity removeAuthorizationRequestEntity(String stateParameter) {
        AuthorizationRequestEntity internalAuthorizationRequestEntity = getInternalAuthorizationRequestEntity(stateParameter);
        if (internalAuthorizationRequestEntity != null) {
            mongoTemplate.remove(
                    Query.query(
                            Criteria.where(LambdaUtils.getPropertyNameForLambda(AuthorizationRequestEntity::getState))
                                    .is(stateParameter)
                    ), AuthorizationRequestEntity.class);
        }
        return internalAuthorizationRequestEntity;
    }
}
