package com.generatera.security.authorization.server.specification;

import com.generatera.security.authorization.server.specification.components.token.JwtClaimsToUserPrincipalMapper;
import com.jianyue.lightning.boot.starter.util.lambda.PropertyNamer;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.generatera.security.authorization.server.specification.Constant.EMPTY_OBJECT;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 12:50
 * @Description Lightning UserPrincipal
 * <p>
 * 任何应用级的用户信息,应该继承于它,并且通过在授权服务器端通过{@link LightningUserPrincipalConverter} 进行实际的  {@code LightningUserPrincipal}
 * 的序列化以及反序列化 ...
 * <p>
 * 在资源服务器端,通过{@link JwtClaimsToUserPrincipalMapper} 转换jwt claims 信息到实际的 {@code LightningUserPrincipal},也就是这两者
 * 所使用的{@code LightningUserPrincipal} 是相同的 ...
 * <p>
 * <p>
 * 对应表单的{@link  org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(String)}
 * 返回的 LightningUserPrincipal 应该保证 {@code isAuthenticated()}返回结果为真 !!!
 * <p>
 * 在应用程序的过程中可以通过扩展框架，实现用户的状态动态控制,来实现对刷新token的影响 - 特别是在jwt token的情况下 ..
 * @see LightningUserPrincipalConverter
 * @see JwtClaimsToUserPrincipalMapper
 */
public interface LightningUserPrincipal extends UserDetails, CredentialsContainer, Serializable {


    default String getName() {
        return getUsername();
    }

    /**
     * 此标识 由框架识别.来决定是否凭证有效 ..
     * <p>
     * 子类可以覆盖,例如可以用这个来使得刷新token 失效 ..
     */
    default boolean isAuthenticated() {
        return isEnabled() && isAccountNonExpired() && isAccountNonLocked() && isCredentialsNonExpired();
    }

    /**
     * 子类可以选择,擦除掉凭证信息,保证账户安全 ..
     * 此方法由框架本身调用,并保持幂等 ..
     */
    @Override
    default void eraseCredentials() {
        // 默认不做任何事情 ..
    }

    /**
     * 主要进行此UserPrincipal的属性获取
     *
     * @param propertyName 属性名
     * @param targetClass  目标类 ..
     */
    default Object getProperty(String propertyName, Class<?> targetClass) {
        Object property = internalGetProperty(propertyName, targetClass);

        if (property == null) {
            AtomicReference<Object> result = new AtomicReference<>();

            ReflectionUtils.doWithMethods(this.getClass(),
                    method -> {
                        if (result.get() == null) {
                            getValueForMethod(result, method);
                            if (method.getDeclaringClass() == this.getClass()) {
                                if (result.get() == null) {
                                    result.set(EMPTY_OBJECT);
                                }
                            }
                        } else {
                            if (method.getDeclaringClass() == this.getClass()) {
                                // 可以覆盖
                                getValueForMethod(result, method);
                                if (result.get() == null) {
                                    result.set(EMPTY_OBJECT);
                                }
                            }
                        }
                    }, method -> {
                        return PropertyNamer.isGetter(method.getName())
                                && !method.isSynthetic()
                                && !method.isBridge()
                                && PropertyNamer.methodToProperty(method.getName()).equals(propertyName.trim())
                                && targetClass.isAssignableFrom(method.getReturnType())
                                && method.getParameterCount() == 0;
                    });

            Object o = result.get();

            // 为空,则根据field 处理 ...
            if (o == null) {
                ReflectionUtils.doWithFields(this.getClass(),
                        field -> {
                            if (result.get() == null) {
                                getValueForField(result, field);
                                if (field.getDeclaringClass() == this.getClass()) {
                                    if (result.get() == null) {
                                        result.set(EMPTY_OBJECT);
                                    }
                                }
                            }
                            else {
                                if (field.getDeclaringClass() == this.getClass()) {
                                    // 可以覆盖
                                    getValueForField(result, field);
                                    if (result.get() == null) {
                                        result.set(EMPTY_OBJECT);
                                    }
                                }
                            }
                        },
                        field -> {
                            return field.getName().equals(propertyName.trim())
                                    && targetClass.isAssignableFrom(field.getType())
                                    && !field.isSynthetic();
                        });
            }
            return o != EMPTY_OBJECT ? o : null;

        }
        return property;
    }

    default Object getProperty(String propertyName) {
        return this.getProperty(propertyName, Object.class);
    }

    private void getValue(AtomicReference<Object> result, Supplier<Object> valueSupplier) {
        result.set(valueSupplier.get());
    }

    private void getValueForMethod(AtomicReference<Object> result, Method method) {
        getValue(result, () -> {
            try {
                ReflectionUtils.makeAccessible(method);
                return method.invoke(this);
            } catch (Exception e) {
                //pass
                return null;
            }
        });
    }

    private void getValueForField(AtomicReference<Object> result, Field field) {
        getValue(result, () -> {
            try {
                ReflectionUtils.makeAccessible(field);
                return field.get(this);
            } catch (Exception e) {
                //pass
                return null;
            }
        });
    }


    /**
     * 自定义扩展 内部快速实现 属性获取 ..
     *
     * @param propertyName 属性名
     * @param targetClass  目标对象
     */
    default Object internalGetProperty(String propertyName, Class<?> targetClass) {
        return null;
    }


    @Override
    default String getPassword() {
        return null;
    }


    @Override
    default boolean isAccountNonExpired() {
        return true;
    }

    @Override
    default boolean isAccountNonLocked() {
        return true;
    }

    @Override
    default boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    default boolean isEnabled() {
        return true;
    }
}

class Constant {
    static Object EMPTY_OBJECT = new Object();
}
