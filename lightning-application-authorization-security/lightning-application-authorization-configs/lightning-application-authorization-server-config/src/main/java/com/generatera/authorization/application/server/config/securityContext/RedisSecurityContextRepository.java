package com.generatera.authorization.application.server.config.securityContext;

import com.generatera.authorization.application.server.config.model.entity.LightningSecurityContextEntity;
import com.generatera.security.application.authorization.server.token.specification.LightningApplicationLevelAuthenticationToken;
import com.generatera.security.server.token.specification.LightningToken;
import com.jianyue.lightning.util.JsonUtil;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author FLJ
 * @date 2023/1/5
 * @time 10:46
 * @Description Redis SecurityContext 仓库
 *
 * 主要目的就是 将LightningUserPrincipal 装载到 SecurityContext中 ...
 *
 * 但是oauth2 应该会有一个Token 校验过程,我们应该先学习一下 ...
 */
public class RedisSecurityContextRepository extends AbstractSecurityContextRepository {

    private final String keyPrefix;

    private final StringRedisTemplate redisTemplate;

    private final Long expiredTimeDuration;

    private Converter<LightningSecurityContextEntity, SecurityContext> toSecurityContextConverter
            = source -> {
                LightningAuthenticationParser parser = LightningAuthenticationParserFactory.loadParser(source.getParserClass());
                return parser.parse(source);
            };

    private Converter<SecurityContext, LightningSecurityContextEntity> fromSecurityContextConverter = source -> {
        LightningAuthentication authentication = (LightningAuthentication) source.getAuthentication();
        LightningAuthenticationParser parser = LightningAuthenticationParserFactory.loadParser(authentication.getParserClass());
        return parser.serialize(source);
    };

    public RedisSecurityContextRepository(String keyPrefix, Long expiredTimeDuration, StringRedisTemplate redisTemplate) {
        Assert.notNull(redisTemplate, "redisTemplate must not be null !!!");
        Assert.hasText(keyPrefix, "keyPrefix must not be blank !!!");
        Assert.isTrue(expiredTimeDuration != null && expiredTimeDuration > 0, "expiredTimeDuration must be gte zero !!!");
        this.keyPrefix = keyPrefix;
        this.redisTemplate = redisTemplate;
        this.expiredTimeDuration = expiredTimeDuration;
    }

    public void setToSecurityContextConverter(Converter<LightningSecurityContextEntity, SecurityContext> securityContextConverter) {
        Assert.notNull(securityContextConverter, "toSecurityContextConverter must not be null !!!");
        this.toSecurityContextConverter = securityContextConverter;
    }

    public void setFromSecurityContextConverter(Converter<SecurityContext, LightningSecurityContextEntity> fromSecurityContextConverter) {
        Assert.notNull(fromSecurityContextConverter, "fromSecurityContextConverter must not be null !!!");
        this.fromSecurityContextConverter = fromSecurityContextConverter;
    }

    @Nullable
    @Override
    protected SecurityContext doLoadContext(String token) {
        String contextToken = redisTemplate.opsForValue()
                .get(constructKey(token));

        if (StringUtils.hasText(contextToken)) {
            return toSecurityContextConverter.convert(JsonUtil.getDefaultJsonUtil().fromJson(contextToken,LightningSecurityContextEntity.class));
        }
        return null;
    }

    // 防止重复
    private String constructKey(String token) {
        return keyPrefix + ".redis.security.context.repository." + token;
    }


    @Override
    protected void doSaveContext(SecurityContext securityContext, LightningApplicationLevelAuthenticationToken authenticationToken) {
        Assert.notNull(authenticationToken.accessToken(), "accessToken must not be null !!!");
        assert authenticationToken.accessToken() != null;
        LightningToken lightningToken = authenticationToken.accessToken();
        assert lightningToken != null;
        assert lightningToken.getTokenValue() != null;
        String key = constructKey(lightningToken.getTokenValue());
        LightningSecurityContextEntity convert = fromSecurityContextConverter.convert(securityContext);
        assert convert != null;
        redisTemplate.opsForValue().set(key, JsonUtil.getDefaultJsonUtil().asJSON(convert), expiredTimeDuration, TimeUnit.MILLISECONDS);
    }
}
