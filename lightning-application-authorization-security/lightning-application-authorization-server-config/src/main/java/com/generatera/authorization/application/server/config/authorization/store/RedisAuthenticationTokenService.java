package com.generatera.authorization.application.server.config.authorization.store;

import com.generatera.authorization.application.server.config.model.entity.ForDBAuthenticationTokenEntity;
import com.generatera.authorization.application.server.config.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.security.authorization.server.specification.LightningUserPrincipalConverter;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningAuthenticationTokenType;
import com.generatera.security.authorization.server.specification.components.token.format.plain.UuidUtil;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import com.jianyue.lightning.util.JsonUtil;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

public class RedisAuthenticationTokenService extends ForDBAuthenticationTokenService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final String keyPrefix;

    private final Long expiredTimeDuration;


    public RedisAuthenticationTokenService(
            String keyPrefix
            , Long expiredTimeDuration,
            LightningUserPrincipalConverter userPrincipalConverter) {
        super(userPrincipalConverter);
        Assert.isTrue(expiredTimeDuration != null && expiredTimeDuration > 0, "expiredTimeDuration must not be null and must gte 0 !!!");
        Assert.hasText(keyPrefix, "keyPrefix must not be null !!!");
        this.keyPrefix = keyPrefix;
        this.expiredTimeDuration = expiredTimeDuration;
    }

    @Override
    protected void doSave(LightningAuthenticationTokenEntity entity) {
        entity.setId(UuidUtil.nextId());
        String id = constructKey(entity.getId());
        redisTemplate.opsForValue()
                .set(id, JsonUtil.getDefaultJsonUtil().asJSON(entity),
                        expiredTimeDuration, TimeUnit.MILLISECONDS
                );

        ElvisUtil.isNotEmptyConsumer(entity.getAccessTokenValue(), token ->
                redisTemplate.opsForValue()
                        .set(constructTokenKey(token, LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE),
                                id, expiredTimeDuration, TimeUnit.MILLISECONDS)
        );

        ElvisUtil.isNotEmptyConsumer(entity.getRefreshTokenValue(), token ->
                redisTemplate.opsForValue()
                        .set(constructTokenKey(token, LightningAuthenticationTokenType.REFRESH_TOKEN_TYPE),
                                id, expiredTimeDuration, TimeUnit.MILLISECONDS)
        );

    }

    private String constructKey(String key) {
        return keyPrefix + key;
    }

    private String constructTokenKey(String token, LightningAuthenticationTokenType tokenType) {
        return constructKey(tokenType.value() + "." + token);
    }


    @Override
    protected void doRemove0(ForDBAuthenticationTokenEntity entity) {
        String key = constructKey(entity.getId());
        redisTemplate.opsForValue().getAndDelete(key);
    }


    @Override
    protected LightningAuthenticationTokenEntity doFindById0(ForDBAuthenticationTokenEntity entity) {
        String value = redisTemplate.opsForValue().get(constructKey(entity.getId()));
        if (StringUtils.hasText(value)) {
            return JsonUtil.getDefaultJsonUtil().fromJson(value, LightningAuthenticationTokenEntity.class);
        }
        return null;
    }

    @Override
    protected LightningAuthenticationTokenEntity doFindAccessOrRefreshTokenByToken(String token) {
        return getTokenForAccessOrRefresh(token, null);
    }

    @Nullable
    private LightningAuthenticationTokenEntity getTokenForAccessOrRefresh(String token, LightningAuthenticationTokenType tokenType) {

        if (tokenType == null) {

            String tokenId = redisTemplate.opsForValue().get(constructTokenKey(token, LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE));

            if (!StringUtils.hasText(tokenId)) {
                tokenId = redisTemplate.opsForValue().get(constructTokenKey(token, LightningAuthenticationTokenType.REFRESH_TOKEN_TYPE));

            }

            if (StringUtils.hasText(tokenId)) {
                String entity = redisTemplate.opsForValue().get(tokenId);
                if (StringUtils.hasText(entity)) {
                    return JsonUtil.getDefaultJsonUtil().fromJson(entity, LightningAuthenticationTokenEntity.class);
                }
            }

        } else {
            String tokenId = redisTemplate.opsForValue().get(constructTokenKey(token, tokenType));
            if (StringUtils.hasText(tokenId)) {
                String value = redisTemplate.opsForValue().get(tokenId);
                if (StringUtils.hasText(value)) {
                    return JsonUtil.getDefaultJsonUtil().fromJson(value, LightningAuthenticationTokenEntity.class);
                } else {
                    return LightningAuthenticationTokenEntity.builder().build();
                }
            }
        }

        return null;
    }

    @Override
    protected LightningAuthenticationTokenEntity doFindByToken0(ForDBAuthenticationTokenEntity entity) {

        if (entity.getAccessTokenType() != null) {
            return getTokenForAccessOrRefresh(entity.getAccessTokenValue(), LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE);
        }

        if (entity.getRefreshTokenType() != null) {
            return getTokenForAccessOrRefresh(entity.getRefreshTokenValue(), LightningAuthenticationTokenType.REFRESH_TOKEN_TYPE);
        }

        return null;
    }
}
