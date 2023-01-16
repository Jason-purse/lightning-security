package com.generatera.authorization.server.common.configuration.authorization.store;

import com.generatera.authorization.server.common.configuration.authorization.DefaultLightningAuthorization;
import com.generatera.authorization.server.common.configuration.authorization.LightningAuthorization;
import com.generatera.authorization.server.common.configuration.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

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
            LightningUserPrincipal userPrincipal = userPrincipalConverter.convert(authorization);
            return DefaultLightningAuthorization
                    .from(authorization)
                    .attribute(LightningAuthorization.USER_INFO_ATTRIBUTE_NAME, userPrincipal)
                    .build();
        }
        return authorization;
    }
}
