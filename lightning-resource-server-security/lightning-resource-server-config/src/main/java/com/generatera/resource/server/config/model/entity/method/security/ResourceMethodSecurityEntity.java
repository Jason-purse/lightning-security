package com.generatera.resource.server.config.model.entity.method.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
 *
 *
 * crud-generic-service 需要重新抽象,不是很好用 ...
 */
@Table(name = "method_resource_security")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceMethodSecurityEntity implements Serializable {

    @Id
    private String id;

    @Column(name = "method_name",length = 255)
    private String methodName;

    /**
     * 方法安全identifier ..
     * 默认通过 类名 + 方法名 + 参数名
     * 来保证唯一性 ...
     */
    @Column(name = "method_security_identifier",length = 255)
    private String methodSecurityIdentifier;

    /**
     * 标识符(简短标识符) ...
     */
    @Column(name = "identifier",length = 255)
    private String identifier;

    /**
     * 执行阶段 ...
     *
     * 方法前 / 或者后 ..
     */
    @Column(name = "invoke_phase",length = 255)
    private String invokePhase;

    @Column(columnDefinition = "text")
    private String roles;

    @Column(columnDefinition = "text")
    private String authorities;


    /**
     * 描述信息...
     */
    @Column(name = "description",columnDefinition = "text")
    private String description;
}
