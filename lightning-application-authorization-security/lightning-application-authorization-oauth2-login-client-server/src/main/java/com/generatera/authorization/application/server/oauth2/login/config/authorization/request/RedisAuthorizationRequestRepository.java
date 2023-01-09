package com.generatera.authorization.application.server.oauth2.login.config.authorization.request;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.AuthorizationRequestEntity;
import com.jianyue.lightning.util.JsonUtil;
import lombok.Setter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Optional;
/**
 * @author FLJ
 * @date 2023/1/9
 * @time 10:15
 * @Description Redis oauth2 authorization Request repository
 */
public class RedisAuthorizationRequestRepository extends AbstractLightningAuthorizationRequestRepository {

    private final static String KEY_FILL_INFO = "lightning.security.oauth2.auth.request.";

    private final StringRedisTemplate redisTemplate;

    private final Long expiredDuration;

    @Setter
    @Nullable
    private String keyPrefix;


    public RedisAuthorizationRequestRepository(StringRedisTemplate redisTemplate
            , Long expiredDuration
            , @Nullable String keyPrefix) {
        Assert.notNull(redisTemplate, "redisTemplate must not be null !!!");
        Assert.notNull(expiredDuration, "expired duration must not be null and must gte zero !!!");
        this.redisTemplate = redisTemplate;
        this.expiredDuration = expiredDuration;
        this.keyPrefix = keyPrefix;

    }

    public RedisAuthorizationRequestRepository(StringRedisTemplate redisTemplate
            , Long expiredDuration) {
        this(redisTemplate, expiredDuration, null);
    }


    @Override
    protected AuthorizationRequestEntity getInternalAuthorizationRequestEntity(String stateParameter) {
        String s = redisTemplate.opsForValue().get(stateParameter);
        if (StringUtils.hasText(s)) {
            return JsonUtil.getDefaultJsonUtil().fromJson(s, AuthorizationRequestEntity.class);
        }
        return null;
    }

    @Override
    protected void saveAuthorizationRequestEntity(AuthorizationRequestEntity entity) {
        redisTemplate.opsForValue().set(
                constructKey(entity.getState()),
                JsonUtil.getDefaultJsonUtil().asJSON(entity),
                expiredDuration
        );
    }

    @Override
    protected AuthorizationRequestEntity removeAuthorizationRequestEntity(String stateParameter) {
        String json = redisTemplate.opsForValue().getAndDelete(constructKey(stateParameter));
        if (StringUtils.hasText(json)) {
            return JsonUtil.getDefaultJsonUtil().fromJson(json, AuthorizationRequestEntity.class);
        }
        return null;
    }

    private String constructKey(String stateParameter) {
        return Optional.ofNullable(keyPrefix)
                .filter(StringUtils::hasText)
                .map(ele -> ele + KEY_FILL_INFO +  stateParameter)
                .orElse(KEY_FILL_INFO + stateParameter);
    }

}
