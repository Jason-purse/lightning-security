package com.generatera.resource.server.config.method.security;
/**
 * @author FLJ
 * @date 2023/3/14
 * @time 9:28
 * @Description 授权模式
 */
public enum AuthorizeMode {

    /**
     * 角色到 权限
     *
     * 那么 roles / authorities 将不会使用 ...
     */
    ROLE_TO_AUTHORITIES,

    /**
     *
     * 权限到角色
     *
     * 那么 roles / authorities 将会使用 ...
     *
     * 这种情况下,authorities 本身的标识可以是系统中的业务处理的相关东西(例如, 权限点可以是 临时访问某个资源的令牌) ...
     *
     *
     * 也就是说这是一种扩展, 在运行时创造的一种 临时令牌,可以加到 对应的资源上,那么资源就可以进行校验 ...
     * // 当临时令牌失效的时候,则需要清理 此资源上的 权限点 ...
     *
     * 这可能需要基于数据库或者其他动态改变资源的配置信息来实现动态处理 ..
     * 并且可以需要连同{@link com.generatera.security.authorization.server.specification.components.token.JwtClaimsToUserPrincipalMapper} 配合
     * 实现对用户资源的实时抓取 ..
     *
     * 但是这种形式可能有些许混乱,自身可能都没有考虑清楚,所以可以看作忽略 ...
     */
    AUTHORITIES_TO_ROLE;
}
