package com.generatera.security.authorization.server.specification.components.annotations;

import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import org.jetbrains.annotations.NotNull;
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
 * @time 16:03
 * @Description 支持对请求头进行解析并注入到  pojo 对象中,详情查看 {@link RequestHeaderArgument} and {@link RequestHeaderInject}
 */
public class RequestHeaderHandlerMethodArgumentResolver extends ModelAttributeMethodProcessor {

    private ConversionService conversionService;

    public RequestHeaderHandlerMethodArgumentResolver() {
        super(false);
    }

    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(RequestHeaderInject.class) != null ||
                AnnotationUtils.findAnnotation(parameter.getParameterType(), RequestHeaderInject.class) != null;
    }

    @Override
    protected void bindRequestParameters(@NotNull WebDataBinder binder, @NotNull NativeWebRequest request) {
        super.bindRequestParameters(binder, request);

        Object target = binder.getTarget();
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        if (target != null) {
            Class<?> aClass = target.getClass();
            ReflectionUtils.doWithFields(aClass, new ReflectionUtils.FieldCallback() {
                @Override
                public void doWith(@NotNull Field field) throws IllegalArgumentException, IllegalAccessException {
                    ReflectionUtils.makeAccessible(field);
                    RequestHeaderArgument annotation = AnnotationUtils.getAnnotation(field, RequestHeaderArgument.class);
                    assert annotation != null;
                    String propertyName = ElvisUtil.stringElvis(annotation.name(), field.getName());
                    String header = request.getHeader(propertyName);
                    if (field.getType().isAssignableFrom(String.class)) {
                        propertyValues.add(field.getName(), header);
                    } else {
                        if (conversionService != null) {
                            Object convert = conversionService.convert(header, field.getType());
                            if (convert != null) {
                                propertyValues.add(field.getName(), convert);
                            }
                        }
                    }
                }
            }, new ReflectionUtils.FieldFilter() {
                @Override
                public boolean matches(@NotNull Field field) {
                    return AnnotationUtils.getAnnotation(field, RequestHeaderArgument.class) != null && !field.isSynthetic() && !field.isEnumConstant();
                }
            });
        }

    }
}
