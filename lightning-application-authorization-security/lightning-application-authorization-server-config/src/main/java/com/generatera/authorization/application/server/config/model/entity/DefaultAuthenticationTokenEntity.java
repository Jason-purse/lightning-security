package com.generatera.authorization.application.server.config.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DefaultAuthenticationTokenEntity extends LightningAuthenticationTokenEntity {
    /**
     * 用户信息,用来更新令牌 ..(如果是 opaque) ..
     * 通过LightningUserPrincipalConverter 处理 ..
     */
    private Object userPrincipal;
}
