package com.generatera.resource.server.config.method.security;

import com.generatera.resource.server.config.method.security.entity.ResourceMethodSecurityEntity;
import com.generatera.resource.server.config.method.security.repository.JpaResourceMethodSecurityRepository;
import com.generatera.security.authorization.server.specification.components.token.format.plain.UuidUtil;
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
 *
 * 如果批量save 出现问题,也就是batch size 达到限制,请使用
 * {spring.jpa.properties.hibernate.jdbc.batch_size} 设置一个系统合适的值 ...
 */
public class JpaPrePostMethodSecurityMetadataSource extends ForDataBasedPrePostMethodSecurityMetadataSource {

    private final JpaResourceMethodSecurityRepository resourceMethodSecurityRepository;

    public JpaPrePostMethodSecurityMetadataSource(PrePostInvocationAttributeFactory attributeFactory,
                                                  JpaResourceMethodSecurityRepository resourceMethodSecurityRepository,
                                                  String moduleName) {
        super(attributeFactory,moduleName);
        Assert.notNull(resourceMethodSecurityRepository,"resource method security must not be null !!!");
        this.resourceMethodSecurityRepository = resourceMethodSecurityRepository;
    }



    @Override
    ResourceMethodSecurityEntity getResourceMethodSecurityEntity(String methodSecurityIdentifier, String invokePhase) {
        return resourceMethodSecurityRepository.findOne(
                Example.of(
                        ResourceMethodSecurityEntity.builder()
                                // 需要加上模块名
                                .moduleName(moduleName)
                                .methodSecurityIdentifier(methodSecurityIdentifier)
                                .invokePhase(invokePhase)
                                // 后端资源
                                .type(ResourceType.BACKEND_TYPE.getType())
                                .build()
                )
        ).orElse(null);
    }

    @Override
    protected void updateResourceMethodSecurityMetadata(List<ResourceMethodSecurityEntity> entities) {

        List<String> list = entities.stream().map(ResourceMethodSecurityEntity::getMethodSecurityIdentifier).toList();

        // 仓库中已经存在的 ..
        List<ResourceMethodSecurityEntity> eles = resourceMethodSecurityRepository.findAllByMethodSecurityIdentifierIn(list);
        
        if(eles != null) {

            // 根据执行阶段进行区分 ...
            Map<String, Map<String, ResourceMethodSecurityEntity>> mapMap = eles.stream().collect(Collectors.groupingBy(ResourceMethodSecurityEntity::getInvokePhase,
                    Collectors.toMap(ResourceMethodSecurityEntity::getMethodSecurityIdentifier, Function.identity())));

            List<ResourceMethodSecurityEntity> needInserts = new LinkedList<>();

            for (ResourceMethodSecurityEntity ele : entities) {

                Map<String, ResourceMethodSecurityEntity> entityMap = mapMap.get(ele.getInvokePhase());
                ResourceMethodSecurityEntity resourceMethodSecurityEntity = null;
                if(entityMap != null) {
                    resourceMethodSecurityEntity = entityMap.get(ele.getMethodSecurityIdentifier());
                }
                // 新增的 ..
                if(resourceMethodSecurityEntity == null) {
                    String s = UuidUtil.nextId();
                    ele.setId(s);
                    needInserts.add(ele);
                    continue;
                }

                // 需要更新的 ..
                if (ResourceMethodSecurityEntity.updateByOptional(resourceMethodSecurityEntity,ele)) {
                    needInserts.add(resourceMethodSecurityEntity);
                }
            }
            // update and save ..
            resourceMethodSecurityRepository.saveAll(needInserts);
        }
        // 全部插入
        else {
            // 全部新增
            resourceMethodSecurityRepository.saveAll(entities);
        }
    }

}
