package com.generatera.authorization.application.server.oauth2.login.config.client;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.ClientRegistrationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@RequiredArgsConstructor
public class MongoClientRegistrationRepository extends AbstractClientRegistrationRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    protected ClientRegistrationEntity internalFindByRegistrationId(String registrationId) {
            return mongoTemplate.findOne(
                    new Query(
                            Criteria.where("registrationId").is(registrationId)
                    ),
                    ClientRegistrationEntity.class
            );
        }
}
