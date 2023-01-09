package com.generatera.authorization.server.common.configuration.authorization.store;

import com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties;
import com.generatera.authorization.server.common.configuration.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.authorization.server.common.configuration.util.HandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class MongoAuthenticationTokenService extends AbstractAuthenticationTokenService {

    @Autowired
    private MongoTemplate mongoTemplate;

    static {
        HandlerFactory.registerHandler(
                new AbstractAuthenticationTokenServiceHandlerProvider() {
                    @Override
                    public boolean support(Object predicate) {
                        return predicate == AuthorizationServerComponentProperties.StoreKind.MONGO;
                    }

                    @Override
                    public HandlerFactory.Handler getHandler() {
                        return new LightningAuthenticationTokenServiceHandler() {
                            @Override
                            public AuthorizationServerComponentProperties.StoreKind getStoreKind() {
                                return AuthorizationServerComponentProperties.StoreKind.MONGO;
                            }

                            @Override
                            public LightningAuthenticationTokenService getService(AuthorizationServerComponentProperties properties) {
                                return new MongoAuthenticationTokenService();
                            }
                        };
                    }
                });
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
