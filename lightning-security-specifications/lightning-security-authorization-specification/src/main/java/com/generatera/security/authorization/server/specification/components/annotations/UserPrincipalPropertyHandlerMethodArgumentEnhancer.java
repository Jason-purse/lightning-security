package com.generatera.security.authorization.server.specification.components.annotations;

import com.generatera.security.authorization.server.specification.LightningUserContext;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import com.jianyue.lightning.boot.starter.util.OptionalFlux;
import com.jianyue.lightning.framework.web.method.argument.context.MethodArgumentContext;
import com.jianyue.lightning.framework.web.method.argument.resolver.enhance.HandlerMethodArgumentEnhancer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessor;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * @author FLJ
 * @date 2023/3/1
 * @time 13:50
 * @Description 支持 UserPrincipalProperty 注入 ...
 * <p>
 * 不支持一个类对象的内嵌复杂对象类型中的 {@link UserPrincipalProperty} 属性解析 !!!!
 */
public class UserPrincipalPropertyHandlerMethodArgumentEnhancer implements HandlerMethodArgumentEnhancer {

    @Nullable
    private ConversionService conversionService;

    public UserPrincipalPropertyHandlerMethodArgumentEnhancer(@Nullable ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public UserPrincipalPropertyHandlerMethodArgumentEnhancer() {
        this.conversionService = new DefaultConversionService();
    }

    public void setConversionService(@Nullable ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    private void getPropertyValues(MutablePropertyValues propertyValues, Object target, ConversionService conversionService, @Nullable String prefix) {
        LightningUserContext.get()
                .getUserPrincipal().ifPresent(ele -> {
                    ReflectionUtils.doWithFields(target.getClass(),
                            new ReflectionUtils.FieldCallback() {
                                @Override
                                public void doWith(@NotNull Field field) throws IllegalArgumentException, IllegalAccessException {
                                    ReflectionUtils.makeAccessible(field);
                                    String name = ElvisUtil.stringElvis(ElvisUtil.isNotEmptyFunction(prefix, ele -> ele.concat(PropertyAccessor.NESTED_PROPERTY_SEPARATOR)), "").concat(field.getName());
                                    UserPrincipalProperty annotation = AnnotationUtils.findAnnotation(field, UserPrincipalProperty.class);
                                    if (annotation != null) {
                                        OptionalFlux
                                                .of(conversionService)
                                                .consume(val -> {
                                                    Object property = ele.getProperty(ElvisUtil.stringElvis(annotation.value(), field.getName()), Object.class);
                                                    addPropertyValue(field, property, propertyValues, conversionService, name);
                                                })
                                                // 否则无参消费
                                                .orElse(
                                                        () -> {
                                                            Object property = ele.getProperty(ElvisUtil.stringElvis(annotation.value(), field.getName()), field.getType());
                                                            addPropertyValue(field, property, propertyValues, conversionService, name);
                                                        }
                                                );
                                    } else {
                                        String nestedPropertyName = ElvisUtil.stringElvis(ElvisUtil.isNotEmptyFunction(prefix, ele -> ele.concat(PropertyAccessor.NESTED_PROPERTY_SEPARATOR)), "").concat(name);
                                        Object nestedProperty = field.get(target);
                                        // 复杂对象, 递归操作 !!!
                                        getPropertyValues(propertyValues, nestedProperty, conversionService, nestedPropertyName);
                                    }

                                }
                            },
                            new ReflectionUtils.FieldFilter() {
                                @Override
                                public boolean matches(@NotNull Field field) {
                                    if (isCommonTypes(field.getType())) {
                                        return AnnotationUtils.findAnnotation(field, UserPrincipalProperty.class) != null;
                                    } else {
                                        return (AnnotationUtils.findAnnotation(field, UserPrincipalInject.class) != null ||
                                                AnnotationUtils.findAnnotation(field.getType(), UserPrincipalInject.class) != null)
                                                && isNotNull(field, target);
                                    }
                                }
                            });
                });
    }


    private boolean isCommonTypes(Class<?> type) {
        return ClassUtils.isPrimitiveOrWrapper(type) || type.isAssignableFrom(String.class) || Collection.class.isAssignableFrom(type);
    }

    private boolean isNotNull(Field field, Object target) {
        ReflectionUtils.makeAccessible(field);
        try {
            return field.get(target) != null;
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    private void addPropertyValue(Field field, Object property, MutablePropertyValues propertyValues, ConversionService conversionService, String fieldName) {
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
    public void enhanceArgument(MethodArgumentContext methodArgumentContext) {

        // 基础类型 ..
        if(isCommonTypes(methodArgumentContext.getMethodParameter().getParameterType())) {
            UserPrincipalProperty parameterAnnotation = methodArgumentContext.getMethodParameter().getParameterAnnotation(UserPrincipalProperty.class);
            assert parameterAnnotation != null;
            String name = parameterAnnotation.name();
            String paramName = ElvisUtil.stringElvis(name, methodArgumentContext.getMethodParameter().getParameterName());
            LightningUserContext.get()
                    .getUserPrincipal().ifPresent(ele -> {
                        // conversion Service 处理 ..
                        if(conversionService != null) {
                            Object property = ele.getProperty(paramName);
                            if(property != null) {
                                if(!methodArgumentContext.getMethodParameter().getParameterType().isAssignableFrom(property.getClass())) {
                                    methodArgumentContext.setTarget(conversionService.convert(property,methodArgumentContext.getMethodParameter().getParameterType()));
                                }
                                else {
                                    // 直接设置
                                    methodArgumentContext.setTarget(property);
                                }
                            }
                        }
                        // 否则直接获取
                        else {
                            methodArgumentContext.setTarget(ele.getProperty(paramName, methodArgumentContext.getMethodParameter().getParameterType()));
                        }
                    });
        }
        else {
            // 复杂类型
            if (methodArgumentContext.getTarget() != null) {
                // 才需要解析 !!!
                MutablePropertyValues propertyValues = new MutablePropertyValues();
                getPropertyValues(propertyValues, methodArgumentContext.getTarget(), conversionService, "");

                BeanWrapperImpl beanWrapper = new BeanWrapperImpl(methodArgumentContext.getTarget());
                beanWrapper.setPropertyValues(propertyValues);
            }
        }

    }

    @Override
    public boolean supportsParameter(@NotNull MethodParameter parameter) {
        return (isCommonTypes(parameter.getParameterType()) && parameter.getParameterAnnotation(UserPrincipalProperty.class) != null) ||
                ((parameter.getParameterAnnotation(UserPrincipalInject.class) != null ||
                        AnnotationUtils.getAnnotation(parameter.getParameterType(), UserPrincipalInject.class) != null));
    }


}
