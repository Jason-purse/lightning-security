package com.generatera.security.authorization.server.specification.components.annotations;

import com.generatera.security.authorization.server.specification.LightningUserContext;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import com.jianyue.lightning.boot.starter.util.OptionalFlux;
import com.jianyue.lightning.framework.web.method.argument.resolver.HttpMessageContext;
import com.jianyue.lightning.framework.web.method.argument.resolver.JsonHttpMessageMethodArgumentResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.function.Predicate;

/**
 * @author FLJ
 * @date 2023/3/1
 * @time 13:50
 * @Description 支持 UserPrincipalProperty 注入 ...
 * <p>
 * 不支持一个类对象的内嵌复杂对象类型中的 {@link UserPrincipalProperty} 属性解析 !!!!
 */
public class UserPrincipalPropertyHandlerMethodArgumentMessageConverter implements JsonHttpMessageMethodArgumentResolver {

    public static final Predicate<MethodParameter> predicate =  parameter ->  parameter.getParameterAnnotation(UserPrincipalInject.class) != null ||
            AnnotationUtils.getAnnotation(parameter.getParameterType(), UserPrincipalInject.class) != null;
    @Nullable
    private ConversionService conversionService;

    public UserPrincipalPropertyHandlerMethodArgumentMessageConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public UserPrincipalPropertyHandlerMethodArgumentMessageConverter() {
        this.conversionService = new DefaultConversionService();
    }

    public void setConversionService(@Nullable ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    private void getPropertyValues(MutablePropertyValues propertyValues, Object target, ConversionService conversionService) {

        ReflectionUtils.doWithFields(target.getClass(),
                new ReflectionUtils.FieldCallback() {
                    @Override
                    public void doWith(@NotNull Field field) throws IllegalArgumentException, IllegalAccessException {
                        String name = field.getName();
                        UserPrincipalProperty annotation = AnnotationUtils.findAnnotation(field, UserPrincipalProperty.class);
                        assert annotation != null;
                        LightningUserContext.get()
                                .getUserPrincipal()
                                .ifPresent(ele -> {
                                    OptionalFlux
                                            .of(conversionService)
                                            .consume(val -> {
                                                Object property = ele.getProperty(ElvisUtil.stringElvis(annotation.value(), name), Object.class);
                                                addPropertyValue(field, property, propertyValues, conversionService);
                                            })
                                            // 否则无参消费
                                            .orElse(
                                                    () -> {
                                                        Object property = ele.getProperty(ElvisUtil.stringElvis(annotation.value(), name), field.getType());
                                                        addPropertyValue(field, property, propertyValues, conversionService);
                                                    }
                                            );
                                });
                    }
                },
                new ReflectionUtils.FieldFilter() {
                    @Override
                    public boolean matches(@NotNull Field field) {
                        return AnnotationUtils.findAnnotation(field, UserPrincipalProperty.class) != null;
                    }
                });
    }

    private void addPropertyValue(Field field, Object property, MutablePropertyValues propertyValues, ConversionService conversionService) {
        String fieldName = field.getName();
        if (property != null) {
            if (!field.getType().isInstance(property)) {
                Object convert = conversionService != null ? conversionService.convert(property, field.getType()) : null;
                if (convert != null) {
                    propertyValues.add(fieldName, convert);
                }
            } else {
                // 保存
                propertyValues.add(fieldName, property);
            }
        }
    }

    @Override
    public Object get(HttpMessageContext httpMessageContext) throws Exception {

        if (httpMessageContext.getTarget() != null) {
            // 才需要解析 !!!
            MutablePropertyValues propertyValues = new MutablePropertyValues();
            getPropertyValues(propertyValues, httpMessageContext.getTarget(), conversionService);

            BeanWrapperImpl beanWrapper = new BeanWrapperImpl(httpMessageContext.getTarget());
            beanWrapper.setPropertyValues(propertyValues);
            return beanWrapper.getWrappedInstance();
        }

        return null;
    }

    @Override
    public boolean supportsParameter(@NotNull MethodParameter parameter) {
       return predicate.test(parameter);
    }
}
