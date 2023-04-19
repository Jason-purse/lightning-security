package com.generatera.authorization.application.server.config.authorization.store;

import com.generatera.authorization.application.server.config.model.entity.ForDBAuthenticationTokenEntity;
import com.generatera.authorization.application.server.config.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.security.authorization.server.specification.LightningUserPrincipalConverter;
import com.jianyue.lightning.boot.starter.util.lambda.LambdaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;

public class MongoAuthenticationTokenService extends ForDBAuthenticationTokenService implements LazyAuthenticationTokenService.TokenClearer {

    @Autowired
    private MongoTemplate mongoTemplate;


    public MongoAuthenticationTokenService(LightningUserPrincipalConverter lightningUserPrincipalConverter) {
        super(lightningUserPrincipalConverter);
    }


    @Override
    protected void doSave0(ForDBAuthenticationTokenEntity entity) {
        mongoTemplate.save(entity);
    }

    @Override
    protected void doRemove0(ForDBAuthenticationTokenEntity entity) {
        mongoTemplate.remove(entity);
    }

    protected ForDBAuthenticationTokenEntity doFindById0(ForDBAuthenticationTokenEntity entity) {
        return mongoTemplate.findOne(
                Query.query(Criteria.where("_id").is(entity.getId())),
                ForDBAuthenticationTokenEntity.class
        );
    }

    @Override
    protected ForDBAuthenticationTokenEntity doFindAccessOrRefreshTokenByToken0(String token) {
        return mongoTemplate.findOne(
                Query.query(
                        Criteria.where("access_token_value").is(token)
                                .orOperator(
                                        Criteria.where("refresh_token_value")
                                                .is(token)
                                )
                ),
                ForDBAuthenticationTokenEntity.class
        );
    }

    @Override
    protected ForDBAuthenticationTokenEntity doFindByToken0(ForDBAuthenticationTokenEntity entity) {
        if (entity.getAccessTokenValue() != null) {
            return mongoTemplate.findOne(
                    Query.query(
                            Criteria.where("access_token_value").is(entity.getAccessTokenValue())
                                    .and("access_token_type").is(entity.getAccessTokenType())
                    ),
                    ForDBAuthenticationTokenEntity.class
            );
        } else if (entity.getRefreshTokenValue() != null) {
            return mongoTemplate.findOne(
                    Query.query(
                            Criteria.where("refresh_token_value")
                                    .is(entity.getRefreshTokenValue())
                                    .and("refresh_token_type")
                                    .is(entity.getRefreshTokenType())),
                    ForDBAuthenticationTokenEntity.class
            );
        }
        // find by token
        return null;
    }

    @Override
    public void clearInvalidToken() {
        // 清理 token
        mongoTemplate.findAllAndRemove(Query.query(
                Criteria.where(LambdaUtils.getPropertyNameForLambda(
                        LightningAuthenticationTokenEntity::getAccessExpiredAt
                )).lte(Instant.now().toEpochMilli())
        ), ForDBAuthenticationTokenEntity.class);
    }
}
