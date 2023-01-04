package com.generatera.authorization.application.server.config.specification.authorization.store;

import com.generatera.authorization.application.server.config.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.authorization.server.common.configuration.token.LightningToken;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import com.jianyue.lightning.boot.starter.util.SnowflakeIdWorker;

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

    private final SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker();

    @Override
    protected void doSave(LightningAuthenticationTokenEntity entity) {
        cache.put(snowflakeIdWorker.nextId(), entity);

        ElvisUtil.isNotEmptyConsumer(entity.getAccessTokenValue(), token -> {
            fastTokenCache.computeIfAbsent(LightningToken.TokenType.ACCESS_TOKEN_TYPE.getTokenTypeString(),
                            key -> new ConcurrentHashMap<>())
                    .put(token, entity);
        });

        ElvisUtil.isNotEmptyConsumer(entity.getRefreshTokenValue(), token -> {
            fastTokenCache.computeIfAbsent(
                    LightningToken.TokenType.REFRESH_TOKEN_TYPE.getTokenTypeString(),
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
        return getTokenByAccessOrRefresh(token,null);
    }

    private LightningAuthenticationTokenEntity getTokenByAccessOrRefresh(String token, LightningToken.TokenType tokenType) {
        if(tokenType == null) {
            LightningAuthenticationTokenEntity entity = fastTokenCache
                    .computeIfAbsent(
                            LightningToken.TokenType.ACCESS_TOKEN_TYPE.getTokenTypeString(),
                            key -> new ConcurrentHashMap<>())
                    .getOrDefault(token, null);
            if (entity == null) {
                return fastTokenCache.computeIfAbsent(
                                LightningToken.TokenType.ACCESS_TOKEN_TYPE.getTokenTypeString(),
                                key -> new ConcurrentHashMap<>())
                        .getOrDefault(token, null);
            }
            return entity;
        }
        else {
            return fastTokenCache
                    .computeIfAbsent(
                            tokenType.getTokenTypeString(),
                            key -> new ConcurrentHashMap<>())
                    .getOrDefault(token, null);
        }
    }

    @Override
    public LightningAuthenticationTokenEntity doFindByToken(LightningAuthenticationTokenEntity entity) {

        if(entity.getAccessTokenValue() != null) {
            return getTokenByAccessOrRefresh(entity.getAccessTokenValue(), LightningToken.TokenType.of(entity.getAccessTokenType()));
        }

        if(entity.getRefreshTokenValue() != null) {
            return getTokenByAccessOrRefresh(entity.getRefreshTokenValue(), LightningToken.TokenType.of(entity.getRefreshTokenType()));
        }

        return null;
    }
}
