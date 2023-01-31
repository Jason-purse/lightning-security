package com.generatera.authorization.application.server.config.authorization.store;

import com.fasterxml.jackson.core.type.TypeReference;
import com.generatera.authorization.application.server.config.authorization.DefaultLightningAuthorization;
import com.generatera.authorization.server.common.configuration.authorization.LightningAuthorization;
import com.generatera.authorization.application.server.config.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.LightningUserPrincipalConverter;
import com.jianyue.lightning.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author FLJ
 * @date 2023/1/16
 * @time 13:12
 * @Description 优化authentication token 处理 ..
 * <p>
 * {@link LightningUserPrincipalConverter} 进行 LightningUserPrincipal 的处理 ...
 */
public class OptimizedAuthenticationTokenConverter extends AuthenticationTokenConverter {

    private LightningUserPrincipalConverter userPrincipalConverter;

    public OptimizedAuthenticationTokenConverter(LightningUserPrincipalConverter userPrincipalConverter) {
        Assert.notNull(userPrincipalConverter, "userPrincipalConverter must not be null !!!");
        this.userPrincipalConverter = userPrincipalConverter;
    }

    public void setUserPrincipalConverter(LightningUserPrincipalConverter userPrincipalConverter) {
        Assert.notNull(userPrincipalConverter, "userPrincipalConverter must not be null !!!");
        this.userPrincipalConverter = userPrincipalConverter;
    }

    @Override
    public DefaultLightningAuthorization convert(@NotNull LightningAuthenticationTokenEntity source) {
        DefaultLightningAuthorization authorization = super.convert(source);
        assert authorization != null;

        Object attribute = authorization.getAttribute(LightningAuthorization.USER_INFO_ATTRIBUTE_NAME);
        if (attribute != null) {
            // 如果被序列化了,直接 反序列化为Map ...
            if(attribute instanceof String) {
                attribute = JsonUtil.getDefaultJsonUtil().fromJson(attribute.toString(), new TypeReference<Map<String,Object>>(){});
            }
            // 否则就是userPrincipal ...
            LightningUserPrincipal userPrincipal = userPrincipalConverter.convert(attribute);
            return DefaultLightningAuthorization
                    .from(authorization)
                    // 复写
                    .attribute(LightningAuthorization.USER_INFO_ATTRIBUTE_NAME, userPrincipal)
                    .build();
        }
        return authorization;
    }
}
