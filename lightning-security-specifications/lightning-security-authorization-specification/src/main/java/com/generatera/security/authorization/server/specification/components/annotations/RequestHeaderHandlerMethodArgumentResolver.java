package com.generatera.security.authorization.server.specification.components.annotations;

import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.annotation.ModelAttributeMethodProcessor;

import java.lang.reflect.Field;
import java.util.function.Predicate;

/**
 * @author FLJ
 * @date 2023/3/1
 * @time 16:03
 * @Description 支持对请求头进行解析并注入到  pojo 对象中,详情查看 {@link RequestHeaderArgument} and {@link RequestHeaderInject}
 */
public class RequestHeaderHandlerMethodArgumentResolver extends ModelAttributeMethodProcessor {
    public static final String DEFAULT_TARGET_CLASS_KEY = "lightning.security.method.resolver.request.header.targetClass";

    public static final RequestHeaderHandlerMethodArgumentResolver INSTANCE = new RequestHeaderHandlerMethodArgumentResolver();
    public static final Predicate<MethodParameter> predicate =  parameter -> (parameter.getParameterAnnotation(RequestHeaderInject.class) != null ||
            AnnotationUtils.findAnnotation(parameter.getParameterType(), RequestHeaderInject.class) != null) ||
            (ClassUtils.isPrimitiveOrWrapper(parameter.getParameterType()) && parameter.getParameterAnnotation(RequestHeaderArgument.class) != null);

    private RequestHeaderHandlerMethodArgumentResolver() {
        super(true);
    }


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return predicate.test(parameter);
    }

    @NotNull
    @Override
    protected Object createAttribute(@NotNull String attributeName, @NotNull MethodParameter parameter, @NotNull WebDataBinderFactory binderFactory, @NotNull NativeWebRequest webRequest) throws Exception {
        if(!ClassUtils.isPrimitiveOrWrapper(parameter.getParameterType()) && !parameter.getParameterType().isAssignableFrom(String.class)) {
            webRequest.setAttribute(DEFAULT_TARGET_CLASS_KEY,parameter, RequestAttributes.SCOPE_REQUEST);
        }
        else {
            // 否则 基础属性,则先尝试 解析 !!
            RequestHeaderArgument annotation = AnnotationUtils.findAnnotation(parameter.getParameter(), RequestHeaderArgument.class);
            assert  annotation != null;
            String header = webRequest.getHeader(annotation.name());
            if(StringUtils.hasText(header)) {
                if(parameter.getParameterType().isAssignableFrom(String.class)) {
                    return header;
                }
                // 则需要转换 !!
                ConversionService conversionService = binderFactory.createBinder(webRequest, "", "").getConversionService();
                if(conversionService != null) {
                    Object convert = conversionService.convert(header, parameter.getParameterType());
                    if(convert != null) {
                        return convert;
                    }
                }

            }
        }
        String value = this.getRequestValueForAttribute(attributeName, webRequest);
        if (value != null) {
            // 手动解析字符串 !!!
            if(parameter.getParameterType().isAssignableFrom(String.class)) {
                return value;
            }

            Object attribute = this.createAttributeFromRequestValue(value, attributeName, parameter, binderFactory, webRequest);
            if (attribute != null) {
                return attribute;
            }
        }
        return super.createAttribute(attributeName, parameter, binderFactory, webRequest);
    }

    @Nullable
    protected String getRequestValueForAttribute(String attributeName, NativeWebRequest request) {
        String parameterValue = request.getParameter(attributeName);
        return StringUtils.hasText(parameterValue) ? parameterValue : null;
    }



    @Nullable
    protected Object createAttributeFromRequestValue(String sourceValue, String attributeName, MethodParameter parameter, WebDataBinderFactory binderFactory, NativeWebRequest request) throws Exception {
        DataBinder binder = binderFactory.createBinder(request, (Object)null, attributeName);
        ConversionService conversionService = binder.getConversionService();
        if (conversionService != null) {
            TypeDescriptor source = TypeDescriptor.valueOf(String.class);
            TypeDescriptor target = new TypeDescriptor(parameter);
            if (conversionService.canConvert(source, target)) {
                return binder.convertIfNecessary(sourceValue, parameter.getParameterType(), parameter);
            }
        }

        return null;
    }

    @Override
    protected void bindRequestParameters(@NotNull WebDataBinder binder, @NotNull NativeWebRequest request) {
        super.bindRequestParameters(binder, request);
        Object attribute = request.getAttribute(DEFAULT_TARGET_CLASS_KEY, RequestAttributes.SCOPE_REQUEST);
        if(attribute != null) {
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
                            ConversionService conversionService = binder.getConversionService();
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
}
