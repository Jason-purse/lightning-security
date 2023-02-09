package com.generatera.resource.server.config.method.security;

import com.generatera.resource.server.config.model.entity.method.security.ResourceMethodSecurityEntity;
import com.generatera.resource.server.config.repository.method.security.JpaResourceMethodSecurityRepository;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.access.prepost.PrePostInvocationAttributeFactory;
import org.springframework.util.Assert;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Override
    protected void updateResourceMethodSecurityMetadata(List<ResourceMethodSecurityEntity> entities) {

        List<String> list = entities.stream().map(ResourceMethodSecurityEntity::getMethodSecurityIdentifier).toList();

        List<ResourceMethodSecurityEntity> eles = resourceMethodSecurityRepository.findAllByMethodSecurityIdentifierIn(list);
        
        if(eles != null) {
            Map<String, Map<String, ResourceMethodSecurityEntity>> mapMap = eles.stream().collect(Collectors.groupingBy(ResourceMethodSecurityEntity::getInvokePhase,
                    Collectors.toMap(ResourceMethodSecurityEntity::getMethodSecurityIdentifier, Function.identity())));

            List<ResourceMethodSecurityEntity> needInserts = new LinkedList<>();
            for (ResourceMethodSecurityEntity ele : entities) {
                Map<String, ResourceMethodSecurityEntity> entityMap = mapMap.get(ele.getInvokePhase());
                ResourceMethodSecurityEntity resourceMethodSecurityEntity = entityMap.get(ele.getMethodSecurityIdentifier());
                if(resourceMethodSecurityEntity == null) {
                    needInserts.add(ele);
                    continue;
                }

                // 更新
                resourceMethodSecurityEntity.setIdentifier(ElvisUtil.stringElvis(resourceMethodSecurityEntity.getIdentifier(),ele.getIdentifier()));
                resourceMethodSecurityEntity.setDescription(ElvisUtil.stringElvis(resourceMethodSecurityEntity.getDescription(),ele.getDescription()));
                resourceMethodSecurityEntity.setRoles(ElvisUtil.stringElvis(resourceMethodSecurityEntity.getRoles(),ele.getRoles()));
                resourceMethodSecurityEntity.setAuthorities(ElvisUtil.stringElvis(resourceMethodSecurityEntity.getAuthorities(),ele.getAuthorities()));
            }

            needInserts.addAll(eles);

            // update and save ..
            resourceMethodSecurityRepository.saveAll(needInserts);
        }
        // 全部插入
        else {
            resourceMethodSecurityRepository.saveAll(entities);
        }
    }
}
