package com.generatera.resource.server.config.method.security;

import com.generatera.resource.server.config.model.entity.method.security.ResourceMethodSecurityEntity;
import com.jianyue.lightning.boot.starter.util.lambda.LambdaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.access.prepost.PrePostInvocationAttributeFactory;

public class MongoPrePostMethodSecurityMetadataSource extends ForDataBasedPrePostMethodSecurityMetadataSource {

    public MongoPrePostMethodSecurityMetadataSource(PrePostInvocationAttributeFactory attributeFactory) {
        super(attributeFactory);
    }

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    ResourceMethodSecurityEntity getResourceMethodSecurityEntity(String methodSecurityIdentifier, String invokePhase) {
        return mongoTemplate.findOne(Query.query(
                        Criteria.where(LambdaUtils.getPropertyNameForLambda(
                                        ResourceMethodSecurityEntity::getMethodSecurityIdentifier
                                )).is(methodSecurityIdentifier)
                                .and(LambdaUtils.getPropertyNameForLambda(ResourceMethodSecurityEntity::getInvokePhase)).is(invokePhase)),
                ResourceMethodSecurityEntity.class);
    }
}
