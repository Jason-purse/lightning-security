package com.generatera.authorization.application.server.config.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


@Table(name = "lightning_token")
@Entity(name = "lightning_token")
@Data
@EqualsAndHashCode(callSuper = true)
public class ForDBAuthenticationTokenEntity extends LightningAuthenticationTokenEntity {

    /**
     * 用户信息,用来更新令牌 ..(如果是 opaque) ..
     * 通过LightningUserPrincipalConverter 处理 ..
     */
    @Column(name = "user_principal",columnDefinition = "text")
    @Field(name = "user_principal")
    private String userPrincipal;
}
