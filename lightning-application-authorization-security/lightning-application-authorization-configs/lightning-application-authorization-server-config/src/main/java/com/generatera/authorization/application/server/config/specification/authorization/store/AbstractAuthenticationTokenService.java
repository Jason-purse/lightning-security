package com.generatera.authorization.application.server.config.specification.authorization.store;

import com.generatera.authorization.application.server.config.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.authorization.application.server.config.specification.LightningAuthenticationTokenService;
import com.generatera.authorization.server.common.configuration.token.LightningAuthenticationToken;
import com.generatera.authorization.server.common.configuration.token.LightningToken;
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

    private Converter<LightningAuthenticationToken, LightningAuthenticationTokenEntity> entityConverter = new AuthenticationTokenEntityConverter();

    private Converter<LightningAuthenticationTokenEntity,LightningAuthenticationToken> tokenConverter = new AuthenticationTokenConverter();

    public void setEntityConverter(Converter<LightningAuthenticationToken, LightningAuthenticationTokenEntity> entityConverter) {
        Assert.notNull(entityConverter,"entityConverter must not be null !!!");
        this.entityConverter = entityConverter;
    }

    public void setTokenConverter(Converter<LightningAuthenticationTokenEntity, LightningAuthenticationToken> tokenConverter) {
        Assert.notNull(entityConverter,"tokenConverter must not be null !!!");
        this.tokenConverter = tokenConverter;
    }

    @Override
    public void save(LightningAuthenticationToken authorization) {
        LightningAuthenticationTokenEntity entity = entityConverter.convert(authorization);
        doSave(entity);
    }

    protected abstract void doSave(LightningAuthenticationTokenEntity entity);

    @Override
    public void remove(LightningAuthenticationToken authorization) {
        LightningAuthenticationTokenEntity entity = entityConverter.convert(authorization);
        doRemove(entity);
    }

    protected abstract void doRemove(LightningAuthenticationTokenEntity entity);

    @Override
    public LightningAuthenticationToken findById(String id) {
        LightningAuthenticationTokenEntity entity = LightningAuthenticationTokenEntity.builder()
                .id(id)
                .build();


        return ElvisUtil.isNotEmptyFunction(doFindById(entity),tokenConverter::convert);
    }

    public abstract LightningAuthenticationTokenEntity doFindById(LightningAuthenticationTokenEntity entity);

    @Override
    public LightningAuthenticationToken findByToken(String token, LightningToken.TokenType tokenType) {
        LightningAuthenticationTokenEntity.LightningAuthenticationTokenEntityBuilder builder
                = LightningAuthenticationTokenEntity.builder();

        if(tokenType == null) {
            // find access / refresh Token
            return ElvisUtil.isNotEmptyFunction(doFindAccessOrRefreshTokenByToken(token),tokenConverter::convert);
        }

        if (tokenType == LightningToken.TokenType.ACCESS_TOKEN_TYPE) {
            return ElvisUtil.isNotEmptyFunction(doFindByToken(
                    builder.accessTokenType(LightningToken.TokenType.ACCESS_TOKEN_TYPE.getTokenTypeString())
                            .accessTokenValue(token)
                            .build()
                    ),
                    tokenConverter::convert
            );
        }

        if(tokenType == LightningToken.TokenType.REFRESH_TOKEN_TYPE) {
            return ElvisUtil.isNotEmptyFunction(
                    doFindByToken(
                            builder.refreshTokenType(LightningToken.TokenType.REFRESH_TOKEN_TYPE.getTokenTypeString())
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
