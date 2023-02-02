package com.generatera.authorization.application.server.oauth2.login.config.client.register;

import com.generatera.authorization.application.server.oauth2.login.config.OAuth2LoginProperties;
import com.generatera.authorization.application.server.oauth2.login.config.model.entity.registration.ClientRegistrationEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author FLJ
 * @date 2023/1/9
 * @time 10:01
 * @Description 抽象模板实现..
 *
 * 提供了 从数据库中获取 客户端注册信息的形式,但是如果通过issuer 进行进一步完整的配置,
 * 每查询一次将导致中央授权服务器压力增加,建议全量配置 所有属性,而不需要依靠 issuer ...
 * 
 * 也就是说,每一次登录都会导致 查询, 如果在增加额外的issuer 请求,消耗更大 ..
 *
 * 同样,对于不需要动态增加 client 注册配置的应用来说, 使用 {@link org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties} 更好
 * 同样,设置 {@link OAuth2LoginProperties#getClientRegistrationStoreKind()} 为Memory 形式...
 *
 *
 * 子类也可以提供缓存策略,来减缓 数据库查询频率,牺牲 client registration 时间 ..
 * 或者通过结合 spring-actuator 来实现缓存刷新端点,进行webhook 回调 ...
 */
public abstract class AbstractClientRegistrationRepository implements LightningOAuth2ClientRegistrationRepository, Iterable<ClientRegistration> {

    private final Converter<ClientRegistrationEntity, ClientRegistration> clientRegistrationConverter = new ClientRegistrationConverter();

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return LightningOAuth2ClientPropertiesRegistrationAdapter.getClientRegistrations(List.of(internalFindByRegistrationId(registrationId))).values().iterator().next();

    }

    protected abstract ClientRegistrationEntity internalFindByRegistrationId(String registrationId);

    protected abstract List<ClientRegistrationEntity> internalFindAllRegistrations();


    @NotNull
    @Override
    public Iterator<ClientRegistration> iterator() {

        List<ClientRegistrationEntity> clientRegistrationEntities = internalFindAllRegistrations();
        if (!CollectionUtils.isEmpty(clientRegistrationEntities)) {
            return LightningOAuth2ClientPropertiesRegistrationAdapter.getClientRegistrations(clientRegistrationEntities).values().iterator();
        }

        return Collections.emptyIterator();
    }
}
