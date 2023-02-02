package com.generatera.authorization.application.server.config.authorization.store;

import com.generatera.authorization.application.server.config.authorization.DefaultLightningAuthorization;
import com.generatera.authorization.server.common.configuration.authorization.LightningAuthorization;
import com.generatera.authorization.application.server.config.model.entity.ForDBAuthenticationTokenEntity;
import com.generatera.authorization.application.server.config.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.LightningUserPrincipalConverter;
import com.jianyue.lightning.boot.starter.util.BeanUtils;
import com.jianyue.lightning.util.JsonUtil;
import org.springframework.util.Assert;

public class OptimizedAuthenticationTokenEntityConverter extends AuthenticationTokenEntityConverter {

    public LightningUserPrincipalConverter userPrincipalConverter;

    public OptimizedAuthenticationTokenEntityConverter(LightningUserPrincipalConverter userPrincipalConverter) {
        Assert.notNull(userPrincipalConverter, "userPrincipalConverter must not be null !!!");
        this.userPrincipalConverter = userPrincipalConverter;
    }

    public void setUserPrincipalConverter(LightningUserPrincipalConverter userPrincipalConverter) {
        Assert.notNull(userPrincipalConverter, "userPrincipalConverter must not be null !!!");
        this.userPrincipalConverter = userPrincipalConverter;
    }

    @Override
    public LightningAuthenticationTokenEntity convert(DefaultLightningAuthorization source) {
        LightningUserPrincipal principal = source.getAttribute(LightningAuthorization.USER_INFO_ATTRIBUTE_NAME);
        LightningAuthenticationTokenEntity entity = super.convert(source);
        ForDBAuthenticationTokenEntity tokenEntity = BeanUtils.transformFrom(entity, ForDBAuthenticationTokenEntity.class);
        Object userPrincipal = userPrincipalConverter.serialize(principal);
        // 总之默认序列化 ...
        assert tokenEntity != null;
        if (userPrincipal instanceof String) {
            tokenEntity.setUserPrincipal(userPrincipal.toString());
        } else {
            tokenEntity.setUserPrincipal(JsonUtil.getDefaultJsonUtil().asJSON(userPrincipal));
        } ;
        return tokenEntity;
    }
}
