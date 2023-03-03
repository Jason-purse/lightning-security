package com.generatera.security.authorization.server.specification;

import com.generatera.security.authorization.server.specification.components.annotations.RequestHeaderHandlerMethodArgumentResolver;
import com.generatera.security.authorization.server.specification.components.annotations.UserPrincipalPropertyHandlerMethodArgumentMessageConverter;
import com.generatera.security.authorization.server.specification.components.annotations.UserPrincipalPropertyHandlerMethodArgumentResolver;
import com.jianyue.lightning.framework.web.method.argument.resolver.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

/**
 * @author FLJ
 * @date 2023/3/3
 * @time 18:16
 * @Description
 */
@AutoConfiguration
@Configuration
public class LightningSecurityAuthorizationSpecificationAutoConfiguration implements FactoryBasedHandlerMethodArgumentResolverConfigurer, FactoryBasedMethodArgumentMessageConverterConfigurer {
    private final UserPrincipalPropertyHandlerMethodArgumentMessageConverter messageConverter = new UserPrincipalPropertyHandlerMethodArgumentMessageConverter();

    @Override
    public void configMethodArgumentResolver(FactoryBasedHandlerMethodArgumentResolver factoryBasedHandlerMethodArgumentResolver) {

        // 处理 UserPrincipalInject 注入问题 !!!
        factoryBasedHandlerMethodArgumentResolver.addArgumentResolverHandlers(

                // @UserPrincipalProperty 注入
                new HandlerMethodArgumentResolverHandlerProvider<>(
                        UserPrincipalPropertyHandlerMethodArgumentResolver.class,
                        new HandlerMethodArgumentResolverHandler() {
                            @Override
                            public Object get(MethodArgumentContext methodArgumentContext) throws Exception {
                                return UserPrincipalPropertyHandlerMethodArgumentResolver.INSTANCE.resolveArgument(
                                        methodArgumentContext.getMethodParameter(),
                                        methodArgumentContext.getMavContainer(),
                                        methodArgumentContext.getRequest(),
                                        methodArgumentContext.getBinderFactory()
                                );
                            }
                        },
                        UserPrincipalPropertyHandlerMethodArgumentResolver.predicate
                ),

                // @RequestHeaderArgument 注入
                new HandlerMethodArgumentResolverHandlerProvider<>(
                        RequestHeaderHandlerMethodArgumentResolver.class,
                        new HandlerMethodArgumentResolverHandler() {
                            @Override
                            public Object get(MethodArgumentContext methodArgumentContext) throws Exception {
                                return RequestHeaderHandlerMethodArgumentResolver.INSTANCE
                                        .resolveArgument(methodArgumentContext.getMethodParameter(),
                                                methodArgumentContext.getMavContainer(),
                                                methodArgumentContext.getRequest(),
                                                methodArgumentContext.getBinderFactory());
                            }
                        },
                        RequestHeaderHandlerMethodArgumentResolver.predicate
                ));
    }

    @Bean
    public UserPrincipalPropertyHandlerMethodArgumentMessageConverter messageConverter(@Autowired(required = false) ConversionService conversionService) {
        if (conversionService != null) {
            messageConverter.setConversionService(conversionService);
        }
        return messageConverter;
    }

    // 配置 消息转码器 ..
    @Override
    public void configure(FactoryBasedMethodArgumentMessageConverter factoryBasedMethodArgumentMessageConverter) {
        factoryBasedMethodArgumentMessageConverter.registerHandlers(new DefaultHandlerMethodArgumentMessageConverter(
                JsonHttpMessageMethodArgumentResolver.class,
                messageConverter.predicate,
                messageConverter
        ));
    }
}
