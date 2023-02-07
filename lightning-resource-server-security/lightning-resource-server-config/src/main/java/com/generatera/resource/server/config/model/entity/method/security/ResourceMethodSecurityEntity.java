package com.generatera.resource.server.config.model.entity.method.security;

import com.jianyue.lightning.boot.starter.generic.crud.service.entity.BaseEntity;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author FLJ
 * @date 2023/2/7
 * @time 10:51
 * @Description 方法资源安全实体
 */
@Table(name = "method_resource_security")
@Entity
@Data
@Builder
public class ResourceMethodSecurityEntity extends BaseEntity implements Serializable {

    @Id
    private String id;

    private String methodName;

    /**
     * 方法安全identifier ..
     * 默认通过 类名 + 方法名 + 参数名 + 注解 上的identifier ....
     * 来保证唯一性 ...
     */
    private String methodSecurityIdentifier;

    /**
     * 执行阶段 ...
     *
     * 方法前 / 或者后 ..
     */
    private String invokePhase;

    @Column(columnDefinition = "text")
    private String roles;

    @Column(columnDefinition = "text")
    private String authorities;

}
