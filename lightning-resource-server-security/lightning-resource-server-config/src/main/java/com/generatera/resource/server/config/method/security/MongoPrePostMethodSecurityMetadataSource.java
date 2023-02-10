package com.generatera.resource.server.config.method.security;

import com.generatera.resource.server.config.method.security.entity.ResourceMethodSecurityEntity;
import com.jianyue.lightning.boot.starter.util.lambda.LambdaUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.access.prepost.PrePostInvocationAttributeFactory;
import org.springframework.util.Assert;

import java.util.List;

public class MongoPrePostMethodSecurityMetadataSource extends ForDataBasedPrePostMethodSecurityMetadataSource {

    public MongoPrePostMethodSecurityMetadataSource(PrePostInvocationAttributeFactory attributeFactory,MongoTemplate mongoTemplate,
                                                    String moduleName) {
        super(attributeFactory,moduleName);
        Assert.notNull(mongoTemplate,"mongoTemplate must not be null !!!");
        this.mongoTemplate = mongoTemplate;
    }

    private final MongoTemplate mongoTemplate;

    @Override
    ResourceMethodSecurityEntity getResourceMethodSecurityEntity(String methodSecurityIdentifier, String invokePhase) {
        return mongoTemplate.findOne(Query.query(
                        Criteria.where(LambdaUtils.getPropertyNameForLambda(
                                        ResourceMethodSecurityEntity::getMethodSecurityIdentifier
                                )).is(methodSecurityIdentifier)
                                .and(LambdaUtils.getPropertyNameForLambda(ResourceMethodSecurityEntity::getInvokePhase)).is(invokePhase)),
                ResourceMethodSecurityEntity.class);
    }


    @Override
    protected void updateResourceMethodSecurityMetadata(List<ResourceMethodSecurityEntity> entities) {
        throw new RuntimeException("未实现 ..");
    }
}
