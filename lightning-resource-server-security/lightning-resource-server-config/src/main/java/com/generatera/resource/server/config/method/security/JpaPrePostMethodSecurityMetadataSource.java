package com.generatera.resource.server.config.method.security;

import com.generatera.resource.server.config.model.entity.method.security.ResourceMethodSecurityEntity;
import com.generatera.resource.server.config.repository.method.security.JpaResourceMethodSecurityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.access.prepost.PrePostInvocationAttributeFactory;
import org.springframework.util.Assert;

/**
 * @author FLJ
 * @date 2023/2/7
 * @time 11:33
 * @Description 进行数据库查询 进而进行 权限获取 ...
 */
public class JpaPrePostMethodSecurityMetadataSource extends ForDataBasedPrePostMethodSecurityMetadataSource {

    private JpaResourceMethodSecurityRepository resourceMethodSecurityRepository;

    public JpaPrePostMethodSecurityMetadataSource(PrePostInvocationAttributeFactory attributeFactory) {
        super(attributeFactory);
    }


    @Autowired
    public void setResourceMethodSecurityRepository(JpaResourceMethodSecurityRepository resourceMethodSecurityRepository) {
        Assert.notNull(resourceMethodSecurityRepository,"resource method security must not be null !!!");
        this.resourceMethodSecurityRepository = resourceMethodSecurityRepository;
    }

    @Override
    ResourceMethodSecurityEntity getResourceMethodSecurityEntity(String methodSecurityIdentifier, String invokePhase) {
        return resourceMethodSecurityRepository.findOne(
                Example.of(
                        ResourceMethodSecurityEntity.builder()
                                .methodSecurityIdentifier(methodSecurityIdentifier)
                                .invokePhase(invokePhase)
                                .build()
                )
        ).orElse(null);
    }
}
