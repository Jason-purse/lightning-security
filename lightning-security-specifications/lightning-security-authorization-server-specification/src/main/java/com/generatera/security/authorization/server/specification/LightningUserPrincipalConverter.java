package com.generatera.security.authorization.server.specification;

import com.generatera.security.authorization.server.specification.components.token.JwtClaimsToUserPrincipalMapper;
import org.jetbrains.annotations.NotNull;

/**
 * @author FLJ
 * @date 2023/1/16
 * @time 11:43
 * @Description 需要一个LightningUserPrincipalConverter ..
 *
 * 必须提供(memory authorization store 默认不需要提供,当然 - 你可以选择覆盖),需要根据字符串形式转换为  一个实际的LightningUserPrincipal ...
 *
 * 一般来说,你需要保证{@link JwtClaimsToUserPrincipalMapper} 产生的
 * {@link LightningUserPrincipal} 是和此转换器使用的 是一致的,
 * 当然 {@link JwtClaimsToUserPrincipalMapper}
 * 产生的 {@link LightningUserPrincipal} 也可以是这个转换器使用的{@code LightningUserPrincipal}的子类 / 子集 ...
 *
 * 因为此转换器使用在授权服务器端, 而 {@link JwtClaimsToUserPrincipalMapper}
 * 使用在资源服务器端 ...
 *
 * @see com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store.service.RedisOAuth2AuthorizationService
 */
public interface LightningUserPrincipalConverter {

    /**
     * 将一个object 转换为 userPrincipal ..
     * @param value LightningUserPrincipal {@link #serialize(LightningUserPrincipal)}的 json 序列化结果 (一般来说是一个map,
     *              但是取决于 authorizationStore 存储类型,如果是 内存形式,它可能就是上述方法返回的结果对象) ..
     *              所以一般需要做判断 ...
     */
    @NotNull
    LightningUserPrincipal convert(@NotNull Object value);

    /**
     * 返回的Object 等价于 convert 方法传入的value
     * 给出一个序列化形式 ...
     */
     Object serialize(LightningUserPrincipal userPrincipal);
}
