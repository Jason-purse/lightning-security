package com.generatera.security.authorization.server.specification;

import org.springframework.util.Assert;

import java.io.Serializable;
/**
 * @author FLJ
 * @date 2023/1/12
 * @time 16:02
 * @Description 客户端认证方法 ..
 *
 * 这个不应该存放在这里 ...
 * 对于普通的授权认证系统来说,不存在客户端认证形式 ...
 * 目前仅仅只有oauth2 才使用这个 ...
 *
 * 所以如果有了oauth2 依赖也不会使用这个 ..
 *
 * @see com.generatera.authorization.server.common.configuration.provider.metadata.AuthorizationServerMetadataEndpointFilter
 */
public final class ClientAuthenticationMethod implements Serializable {


    /**
     * 目前支持 basic
     */
    @Deprecated
    public static final ClientAuthenticationMethod BASIC = new ClientAuthenticationMethod("client_secret_basic");
    /**
     * post
     */
    public static final ClientAuthenticationMethod POST = new ClientAuthenticationMethod("client_secret_post");

    private final String value;

    public ClientAuthenticationMethod(String value) {
        Assert.hasText(value, "value cannot be empty");
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            ClientAuthenticationMethod that = (ClientAuthenticationMethod)obj;
            return this.getValue().equalsIgnoreCase(that.getValue());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.getValue().hashCode();
    }
}