package com.generatera.authorization.server.common.configuration.authorization.store;

import com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties;
import com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties.StoreKind;
import com.generatera.authorization.server.common.configuration.authorization.DefaultLightningAuthorization;
import com.generatera.authorization.server.common.configuration.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.authorization.server.common.configuration.util.HandlerFactory;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningAuthenticationTokenType;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.Assert;
/**
 * @author FLJ
 * @date 2023/1/4
 * @time 14:33
 * @Description 抽象的 认证Token 管理 service
 */
public abstract class AbstractAuthenticationTokenService implements LightningAuthenticationTokenService {

    private Converter<DefaultLightningAuthorization, LightningAuthenticationTokenEntity> entityConverter = new AuthenticationTokenEntityConverter();

    private Converter<LightningAuthenticationTokenEntity, DefaultLightningAuthorization> tokenConverter = new AuthenticationTokenConverter();


    public void setEntityConverter(Converter<DefaultLightningAuthorization, LightningAuthenticationTokenEntity> entityConverter) {
        Assert.notNull(entityConverter,"entityConverter must not be null !!!");
        this.entityConverter = entityConverter;
    }

    public void setTokenConverter(Converter<LightningAuthenticationTokenEntity, DefaultLightningAuthorization> tokenConverter) {
        Assert.notNull(entityConverter,"tokenConverter must not be null !!!");
        this.tokenConverter = tokenConverter;
    }

    @Override
    public void save(DefaultLightningAuthorization authorization) {
        LightningAuthenticationTokenEntity entity = entityConverter.convert(authorization);
        doSave(entity);
    }

    protected abstract void doSave(LightningAuthenticationTokenEntity entity);

    @Override
    public void remove(DefaultLightningAuthorization authorization) {
        LightningAuthenticationTokenEntity entity = entityConverter.convert(authorization);
        doRemove(entity);
    }

    protected abstract void doRemove(LightningAuthenticationTokenEntity entity);

    @Override
    public DefaultLightningAuthorization findAuthorizationById(String id) {
        LightningAuthenticationTokenEntity entity = LightningAuthenticationTokenEntity.builder()
                .id(id)
                .build();


        return ElvisUtil.isNotEmptyFunction(doFindById(entity),tokenConverter::convert);
    }

    public abstract LightningAuthenticationTokenEntity doFindById(LightningAuthenticationTokenEntity entity);

    @Override
    public DefaultLightningAuthorization findByToken(String token, LightningAuthenticationTokenType tokenType) {
        LightningAuthenticationTokenEntity.LightningAuthenticationTokenEntityBuilder builder
                = LightningAuthenticationTokenEntity.builder();

        if(tokenType == null) {
            // find access / refresh Token
            return ElvisUtil.isNotEmptyFunction(doFindAccessOrRefreshTokenByToken(token),tokenConverter::convert);
        }

        if (tokenType == LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE) {
            return ElvisUtil.isNotEmptyFunction(doFindByToken(
                    builder.accessTokenType(LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE.value())
                            .accessTokenValue(token)
                            .build()
                    ),
                    tokenConverter::convert
            );
        }

        if(tokenType == LightningAuthenticationTokenType.REFRESH_TOKEN_TYPE) {
            return ElvisUtil.isNotEmptyFunction(
                    doFindByToken(
                            builder.refreshTokenType(LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE.value())
                                    .refreshTokenValue(token)
                                    .build()
                    ),
                    tokenConverter::convert
            );
        }

        // other null
        return null;
    }

    /**
     * 获取访问Token 或者 刷新Token
     * @param token token Value
     */
    protected abstract LightningAuthenticationTokenEntity doFindAccessOrRefreshTokenByToken(String token);

    public abstract LightningAuthenticationTokenEntity doFindByToken(LightningAuthenticationTokenEntity entity);
}
