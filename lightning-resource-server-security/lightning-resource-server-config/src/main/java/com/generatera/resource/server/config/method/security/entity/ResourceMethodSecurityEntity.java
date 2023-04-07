package com.generatera.resource.server.config.method.security.entity;

import com.jianyue.lightning.boot.starter.util.OptionalFlux;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

import static com.jianyue.lightning.boot.starter.util.PredicateUtil.nullSafeEquals;

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
    @Column(name = "module_name", length = 255)
    private String moduleName;

    @Column(name = "method_name", length = 255)
    private String methodName;

    /**
     * 方法安全identifier ..
     * 默认通过 类名 + 方法名 + 参数名
     * 来保证唯一性 ...(也就是作为 权限点 唯一标识) ...
     */
    @Column(name = "method_security_identifier", length = 255)
    private String methodSecurityIdentifier;

    /**
     * 标识符(简短标识符) ...
     * 前端可展示的 (用于运营平台 进行权限绑定) ...
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

    /**
     * 应该是让权限点和角色绑定 ... 而不是角色绑定到权限点 ..
     */
    @Deprecated
    @Column(columnDefinition = "text")
    private String roles;

    /**
     * 应该是让权限点和角色绑定 ... 而不是角色绑定到权限点 ..
     */
    @Deprecated
    @Column(columnDefinition = "text")
    private String authorities;


    /**
     * 描述信息...
     */
    @Column(name = "description", columnDefinition = "text")
    private String description;

    /**
     * 资源类型 ...
     *
     * @see com.generatera.resource.server.config.method.security.ResourceType
     */
    @Column(name = "type")
    private String type;


    /**
     * 资源行为
     *
     * @see com.generatera.resource.server.config.method.security.ResourceBehavior
     * <p>
     * 读或者写 / 或者读写
     */
    @Column(name = "behavior")
    private String behavior;


    @Column(name = "authorize_mode")
    private String authorizeMode;

    /**
     * 选择性更新
     * true,表示更新了,false 表示没有 ...
     */
    public static boolean updateByOptional(ResourceMethodSecurityEntity one, ResourceMethodSecurityEntity two) {

        return OptionalFlux
                .stringOrNull(two.identifier)
                .switchMapIfFalseOrNull(nullSafeEquals(one.identifier), identifier -> {
                    one.identifier = identifier;
                    return Boolean.TRUE;
                })
                .combine(
                        OptionalFlux.stringOrNull(two.description)
                                .switchMapIfFalseOrNull(nullSafeEquals(one.description), description -> {
                                    one.description = description;
                                    return Boolean.TRUE;
                                }),
                        Boolean::logicalOr
                )

                .combine(
                        OptionalFlux.stringOrNull(two.roles)
                                .switchMapIfFalseOrNull(nullSafeEquals(one.roles), roles -> {
                                    one.roles = roles;
                                    return Boolean.TRUE;
                                }),
                        Boolean::logicalOr
                )

                .combine(
                        OptionalFlux.stringOrNull(two.authorities)
                                .switchMapIfFalseOrNull(nullSafeEquals(one.authorities), authorities -> {
                                    one.authorities = authorities;
                                    return Boolean.TRUE;
                                }),
                        Boolean::logicalOr
                )

                .combine(
                        OptionalFlux.stringOrNull(two.behavior)
                                .switchMapIfFalseOrNull(nullSafeEquals(one.behavior), behavior -> {
                                    one.behavior = behavior;
                                    return Boolean.TRUE;
                                }),
                        Boolean::logicalOr
                )

                .combine(
                        OptionalFlux.stringOrNull(two.type)
                                .switchMapIfFalseOrNull(nullSafeEquals(one.type), type -> {
                                    one.type = type;
                                    return Boolean.TRUE;
                                }),
                        Boolean::logicalOr
                )
                .orElse(Boolean.FALSE)
                .getResult();
    }


}
