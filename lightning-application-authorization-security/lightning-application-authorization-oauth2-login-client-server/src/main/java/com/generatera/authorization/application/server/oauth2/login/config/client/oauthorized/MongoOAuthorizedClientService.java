package com.generatera.authorization.application.server.oauth2.login.config.client.oauthorized;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.OAuthorizedClientEntity;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;

public class MongoOAuthorizedClientService extends AbstractOAuth2OAuthorizedClientService {
    public MongoOAuthorizedClientService(MongoTemplate mongoTemplate) {
        Assert.notNull(mongoTemplate,"mongoTemplate must not be null !!!");
        this.mongoTemplate = mongoTemplate;
    }

    private final MongoTemplate mongoTemplate;

    @Override
    protected OAuthorizedClientEntity internalLoadAuthorizedClient(String clientRegistrationId, String principalName) {
        return mongoTemplate.findOne(
                Query.query(
                        Criteria.where("clientRegistrationId")
                                .is(clientRegistrationId)
                                .and("principalName")
                                .is(principalName)
                ),
                OAuthorizedClientEntity.class
        );
    }

    @Override
    protected void internalSaveAuthorizedClient(OAuthorizedClientEntity oAuthorizedClientEntity) {
        mongoTemplate.save(oAuthorizedClientEntity);
    }

    @Override
    protected void internalRemoveAuthorizedClient(OAuthorizedClientEntity entity) {
        mongoTemplate.remove(entity);
    }
}
