package com.generatera.authorization.server.common.configuration.authorization.store;

import com.generatera.authorization.server.common.configuration.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningAuthenticationTokenType;
import com.generatera.security.authorization.server.specification.components.token.format.plain.UuidUtil;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author FLJ
 * @date 2023/1/4
 * @time 15:11
 * @Description 基于内存的 AuthenticationTokenService
 */
public class DefaultAuthenticationTokenService extends AbstractAuthenticationTokenService {

    private final Map<String, LightningAuthenticationTokenEntity> cache = new ConcurrentHashMap<>();

    private final Map<String, Map<String, LightningAuthenticationTokenEntity>> fastTokenCache = new ConcurrentHashMap<>();


    @Override
    protected void doSave(LightningAuthenticationTokenEntity entity) {
        cache.put(UuidUtil.nextId(), entity);

        ElvisUtil.isNotEmptyConsumer(entity.getAccessTokenValue(), token -> {
            fastTokenCache.computeIfAbsent(LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE.value(),
                            key -> new ConcurrentHashMap<>())
                    .put(token, entity);
        });

        ElvisUtil.isNotEmptyConsumer(entity.getRefreshTokenValue(), token -> {
            fastTokenCache.computeIfAbsent(
                    LightningAuthenticationTokenType.REFRESH_TOKEN_TYPE.value(),
                    key -> new ConcurrentHashMap<>()
            ).put(token, entity);
        });

    }

    @Override
    protected void doRemove(LightningAuthenticationTokenEntity entity) {
        cache.remove(entity.getId());
    }

    @Override
    public LightningAuthenticationTokenEntity doFindById(LightningAuthenticationTokenEntity entity) {
        return cache.get(entity.getId());
    }

    @Override
    protected LightningAuthenticationTokenEntity doFindAccessOrRefreshTokenByToken(String token) {
        return getTokenByAccessOrRefresh(token, null);
    }

    private LightningAuthenticationTokenEntity getTokenByAccessOrRefresh(String token, LightningAuthenticationTokenType tokenType) {
        if (tokenType == null) {
            LightningAuthenticationTokenEntity entity = fastTokenCache
                    .computeIfAbsent(
                            LightningAuthenticationTokenType.REFRESH_TOKEN_TYPE.value(),
                            key -> new ConcurrentHashMap<>())
                    .getOrDefault(token, null);
            if (entity == null) {
                return fastTokenCache.computeIfAbsent(
                                LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE.value(),
                                key -> new ConcurrentHashMap<>())
                        .getOrDefault(token, null);
            }
            return entity;
        } else {
            return fastTokenCache
                    .computeIfAbsent(
                            tokenType.value(),
                            key -> new ConcurrentHashMap<>())
                    .getOrDefault(token, null);
        }
    }

    @Override
    public LightningAuthenticationTokenEntity doFindByToken(LightningAuthenticationTokenEntity entity) {

        if (entity.getAccessTokenValue() != null) {
            return getTokenByAccessOrRefresh(entity.getAccessTokenValue(), LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE);
        }

        if (entity.getRefreshTokenValue() != null) {
            return getTokenByAccessOrRefresh(entity.getRefreshTokenValue(), LightningAuthenticationTokenType.REFRESH_TOKEN_TYPE);
        }

        return null;
    }
}
