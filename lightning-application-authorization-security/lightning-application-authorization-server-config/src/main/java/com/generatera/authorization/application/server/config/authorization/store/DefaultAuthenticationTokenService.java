package com.generatera.authorization.application.server.config.authorization.store;

import com.generatera.authorization.application.server.config.authorization.DefaultLightningAuthorization;
import com.generatera.authorization.application.server.config.model.entity.DefaultAuthenticationTokenEntity;
import com.generatera.authorization.application.server.config.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.authorization.server.common.configuration.authorization.LightningAuthorization;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.LightningUserPrincipalConverter;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningAuthenticationTokenType;
import com.generatera.security.authorization.server.specification.components.token.format.plain.UuidUtil;
import com.jianyue.lightning.boot.starter.util.BeanUtils;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import com.jianyue.lightning.boot.starter.util.OptionalFlux;
import com.jianyue.lightning.boot.starter.util.dataflow.impl.Tuple;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author FLJ
 * @date 2023/1/4
 * @time 15:11
 * @Description 基于内存的 AuthenticationTokenService
 * <p>
 * 默认不需要 {@link LightningUserPrincipalConverter}, 但是你也可以覆盖 ...
 */
public class DefaultAuthenticationTokenService extends AbstractAuthenticationTokenService {

    private final Long expireTimeDuration;

    public DefaultAuthenticationTokenService(Long expireTimeDuration) {
        super(defaultConverter());
        Assert.isTrue(expireTimeDuration != null && expireTimeDuration > 0, "expireTimeDuration must not be null and gte zero !!!");
        setEntityConverter(new OptimizedAuthenticationTokenEntityConverter(defaultConverter()) {
            @Override
            public LightningAuthenticationTokenEntity convert(DefaultLightningAuthorization source) {
                LightningAuthenticationTokenEntity tokenEntity = super.convert(source);
                DefaultAuthenticationTokenEntity entity
                        = BeanUtils.transformFrom(tokenEntity, DefaultAuthenticationTokenEntity.class);

                assert entity != null;
                // 直接放入
                entity.setUserPrincipal(source.getAttribute(LightningAuthorization.USER_INFO_ATTRIBUTE_NAME));

                return entity;
            }
        });

        this.expireTimeDuration = expireTimeDuration;
    }

    /**
     * 同步 ..
     */
    private synchronized void clearTokens() {
        long currentTime = Instant.now().getEpochSecond();
        List<String> keys = new LinkedList<>();
        for (Map.Entry<String, Tuple<LightningAuthenticationTokenEntity, Long>> entry : cache.entrySet()) {
            Long second = entry.getValue().getSecond();
            if (currentTime <= second) {
                keys.add(entry.getKey());
            }
        }

        // 删除
        for (String key : keys) {
            Tuple<LightningAuthenticationTokenEntity, Long> value = cache.remove(key);

            // 快速cache 中删除 ..
            if(value != null) {
                fastTokenCache.get(LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE.value())
                        .remove(value.getFirst().getAccessTokenValue());

                fastTokenCache.get(LightningAuthenticationTokenType.REFRESH_TOKEN_TYPE.value());
            }
        }
    }

    private final Map<String, Tuple<LightningAuthenticationTokenEntity, Long>> cache = new ConcurrentHashMap<>();

    private final Map<String, Map<String, LightningAuthenticationTokenEntity>> fastTokenCache = new ConcurrentHashMap<>();


    public static LightningUserPrincipalConverter defaultConverter() {
        return // 默认不做任何事情 ..
                new LightningUserPrincipalConverter() {
                    @NotNull
                    @Override
                    public LightningUserPrincipal convert(@NotNull Object value) {
                        //
                        return ((LightningUserPrincipal) value);
                    }

                    @Override
                    public Object serialize(LightningUserPrincipal userPrincipal) {
                        return userPrincipal;
                    }
                };
    }

    @Override
    protected void doSave(LightningAuthenticationTokenEntity entity) {

        // 做清理
        clearTokens();

        cache.put(UuidUtil.nextId(), new Tuple<>(entity, Instant.now().plusMillis(expireTimeDuration).getEpochSecond()));

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
        // 做清理
        clearTokens();
        cache.remove(entity.getId());
    }

    @Override
    public LightningAuthenticationTokenEntity doFindById(LightningAuthenticationTokenEntity entity) {
        // 做清理
        clearTokens();
        return OptionalFlux.of(cache.get(entity.getId())).map(Tuple::getFirst).getResult();
    }

    @Override
    protected LightningAuthenticationTokenEntity doFindAccessOrRefreshTokenByToken(String token) {
        clearTokens();
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
        clearTokens();
        if (entity.getAccessTokenValue() != null) {
            return getTokenByAccessOrRefresh(entity.getAccessTokenValue(), LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE);
        }

        if (entity.getRefreshTokenValue() != null) {
            return getTokenByAccessOrRefresh(entity.getRefreshTokenValue(), LightningAuthenticationTokenType.REFRESH_TOKEN_TYPE);
        }

        return null;
    }
}
