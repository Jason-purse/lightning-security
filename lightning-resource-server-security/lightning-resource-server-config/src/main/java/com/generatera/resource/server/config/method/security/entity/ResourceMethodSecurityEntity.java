package com.generatera.resource.server.config.method.security.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

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
 * <p>
 * <p>
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

    /**
     * 模块名称 ..
     */
    @Column(name = "module_name",length = 255)
    private String moduleName;

    @Column(name = "method_name", length = 255)
    private String methodName;

    /**
     * 方法安全identifier ..
     * 默认通过 类名 + 方法名 + 参数名
     * 来保证唯一性 ...
     */
    @Column(name = "method_security_identifier", length = 255)
    private String methodSecurityIdentifier;

    /**
     * 标识符(简短标识符) ...
     */
    @Column(name = "identifier", length = 255)
    private String identifier;

    /**
     * 执行阶段 ...
     * <p>
     * 方法前 / 或者后 ..
     */
    @Column(name = "invoke_phase", length = 255)
    private String invokePhase;

    @Column(columnDefinition = "text")
    private String roles;

    @Column(columnDefinition = "text")
    private String authorities;


    /**
     * 描述信息...
     */
    @Column(name = "description", columnDefinition = "text")
    private String description;


    /**
     * 选择性更新
     * true,表示更新了,false 表示没有 ...
     */
    public static boolean updateByOptional(ResourceMethodSecurityEntity one, ResourceMethodSecurityEntity two) {
        boolean status = false;
        if(!StringUtils.hasText(one.identifier)) {
            one.identifier = two.identifier;
            status = true;
        }

        if(!StringUtils.hasText(one.description)) {
            one.description = two.description;
            status = true;
        }

        if(!StringUtils.hasText(one.roles)) {
            one.roles = two.roles;
            status = true;
        }

        if(!StringUtils.hasText(one.authorities)) {
            one.authorities = two.authorities;
            status = true;
        }

        return false;
    }


}
