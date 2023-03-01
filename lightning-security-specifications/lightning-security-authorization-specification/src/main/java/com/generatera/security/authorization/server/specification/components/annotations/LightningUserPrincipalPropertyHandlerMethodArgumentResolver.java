package com.generatera.security.authorization.server.specification.components.annotations;

import com.generatera.security.authorization.server.specification.LightningUserContext;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import com.jianyue.lightning.boot.starter.util.OptionalFlux;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.ModelAttributeMethodProcessor;

import java.lang.reflect.Field;

/**
 * @author FLJ
 * @date 2023/3/1
 * @time 13:50
 * @Description 支持 UserPrincipalProperty 注入 ...
 *
 * 不支持一个类对象的内嵌复杂对象类型中的 {@link UserPrincipalProperty} 属性解析 !!!!
 */
public class LightningUserPrincipalPropertyHandlerMethodArgumentResolver extends ModelAttributeMethodProcessor {
    @Nullable
    private ConversionService conversionService;

    public LightningUserPrincipalPropertyHandlerMethodArgumentResolver() {
        super(true);
    }

    public void setConversionService(@Nullable ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(UserPrincipalInject.class) != null ||
                AnnotationUtils.getAnnotation(parameter.getParameterType(), UserPrincipalInject.class) != null;
    }

    @Override
    protected void bindRequestParameters(@NotNull WebDataBinder binder, @NotNull NativeWebRequest request) {
        super.bindRequestParameters(binder, request);
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        Object target = binder.getTarget();
        if (target != null) {
            getPropertyValues(propertyValues, target);

            // 绑定属性值 ...
            binder.bind(propertyValues);
        }
    }

    private void getPropertyValues(MutablePropertyValues propertyValues, Object target) {

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
                                                addPropertyValue(field, property, propertyValues);
                                            })
                                            // 否则无参消费
                                            .orElse(
                                                    () -> {
                                                        Object property = ele.getProperty(ElvisUtil.stringElvis(annotation.value(), name), field.getType());
                                                        addPropertyValue(field, property, propertyValues);
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

    private void addPropertyValue(Field field, Object property, MutablePropertyValues propertyValues) {
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
}
