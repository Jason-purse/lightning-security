package com.generatera.oauth2.resource.server.config.token;

import com.generatera.security.authorization.server.specification.components.token.format.jwt.LightningOpaqueTokenIntrospector;
/**
 * @author FLJ
 * @date 2023/1/12
 * @time 16:59
 * @Description oauth2 opaque token ...
 *
 *
 * todo 当 central-oauth2-authorization-server 本身就是 资源服务器,我们直接调用对应的资源进行 校验即可 ..
 *
 *
 */
public interface LightningOAuth2OpaqueTokenIntrospector extends LightningOpaqueTokenIntrospector  {

}
