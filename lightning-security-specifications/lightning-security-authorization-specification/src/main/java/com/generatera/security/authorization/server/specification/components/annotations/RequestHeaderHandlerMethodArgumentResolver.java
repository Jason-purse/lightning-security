package com.generatera.security.authorization.server.specification.components.annotations;

import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import com.jianyue.lightning.framework.web.method.argument.context.MethodArgumentContext;
import com.jianyue.lightning.framework.web.method.argument.resolver.enhance.HandlerMethodArgumentEnhancer;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;

import java.lang.reflect.Field;

/**
 * @author FLJ
 * @date 2023/3/1
 * @time 16:03
 * @Description 支持对请求头进行解析并注入到  pojo 对象中,详情查看 {@link RequestHeaderArgument} and {@link RequestHeaderInject}
 */
public class RequestHeaderHandlerMethodArgumentResolver implements HandlerMethodArgumentEnhancer {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return (parameter.getParameterAnnotation(RequestHeaderInject.class) != null ||
                AnnotationUtils.findAnnotation(parameter.getParameterType(), RequestHeaderInject.class) != null) ||
                (ClassUtils.isPrimitiveOrWrapper(parameter.getParameterType()) && parameter.getParameterAnnotation(RequestHeaderArgument.class) != null);
    }
    @Override
    public void enhanceArgument(MethodArgumentContext methodArgumentContext) {
        MethodParameter parameter = methodArgumentContext.getMethodParameter();
        NativeWebRequest webRequest = methodArgumentContext.getRequest();
        WebDataBinderFactory binderFactory = methodArgumentContext.getBinderFactory();
        // 否则 基础属性,则先尝试 解析 !!
        RequestHeaderArgument annotation = AnnotationUtils.findAnnotation(parameter.getParameter(), RequestHeaderArgument.class);
        assert  annotation != null;
        String header = webRequest.getHeader(annotation.name());
        if(StringUtils.hasText(header)) {
            if(ClassUtils.isPrimitiveOrWrapper(parameter.getParameterType()) || parameter.getParameterType().isAssignableFrom(String.class)) {
                if(parameter.getParameterType().isAssignableFrom(String.class)) {
                    methodArgumentContext.setTarget(header);
                    return ;
                }
                try {
                    // 则需要转换 !!
                    assert binderFactory != null;
                    ConversionService conversionService = binderFactory.createBinder(webRequest, "", "").getConversionService();
                    if(conversionService != null) {
                        Object convert = conversionService.convert(header, parameter.getParameterType());
                        if(convert != null) {
                            methodArgumentContext.setTarget(convert);
                        }
                    }
                }catch (Exception e) {
                    // pass
                }
            }
            else {
               if(methodArgumentContext.getTarget() != null) {
                   MutablePropertyValues propertyValues = new MutablePropertyValues();
                   Class<?> aClass = methodArgumentContext.getTarget().getClass();
                  try {
                      assert binderFactory != null;
                      WebDataBinder binder = binderFactory.createBinder(webRequest, methodArgumentContext.getTarget(), "");
                      ReflectionUtils.doWithFields(aClass, new ReflectionUtils.FieldCallback() {
                          @Override
                          public void doWith(@NotNull Field field) throws IllegalArgumentException, IllegalAccessException {
                              ReflectionUtils.makeAccessible(field);
                              RequestHeaderArgument annotation = AnnotationUtils.getAnnotation(field, RequestHeaderArgument.class);
                              assert annotation != null;
                              String propertyName = ElvisUtil.stringElvis(annotation.name(), field.getName());
                              String header = webRequest.getHeader(propertyName);
                              if (field.getType().isAssignableFrom(String.class)) {
                                  propertyValues.add(field.getName(), header);
                              } else {
                                  try {
                                      ConversionService conversionService = binder.getConversionService();
                                      if (conversionService != null) {
                                          Object convert = conversionService.convert(header, field.getType());
                                          if (convert != null) {
                                              propertyValues.add(field.getName(), convert);
                                          }
                                      }
                                  }catch (Exception e) {
                                      // pass
                                  }

                              }
                          }
                      }, new ReflectionUtils.FieldFilter() {
                          @Override
                          public boolean matches(@NotNull Field field) {
                              return AnnotationUtils.getAnnotation(field, RequestHeaderArgument.class) != null && !field.isSynthetic() && !field.isEnumConstant();
                          }
                      });

                      // 绑定 !!!
                      binder.bind(propertyValues);
                  }catch (Exception e) {
                      // pass
                  }
               }
            }

        }
    }



}
