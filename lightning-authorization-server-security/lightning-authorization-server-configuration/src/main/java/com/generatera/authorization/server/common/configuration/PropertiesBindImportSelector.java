package com.generatera.authorization.server.common.configuration;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 *  属性绑定的 Import Selector ..
 *
 *  import selector 的执行是直接实例化的,没有完整的bean生命周期流程 ..
 *  但是可以构造器注入以下类型的 bean ..
 *  1. BeanFactory
 *  2. Environment
 *  3. ResourceLoader
 *  4. ClassLoader
 *
 */
public abstract class PropertiesBindImportSelector<T> implements ImportSelector {

    private final T properties;

    @SuppressWarnings("unchecked")
    public PropertiesBindImportSelector(
            BeanFactory beanFactory,
            Environment environment
    ) {

        ResolvableType type =
                ResolvableType.forClass(this.getClass())
                .as(PropertiesBindImportSelector.class)
                .getGeneric();
        Assert.isTrue(type != ResolvableType.NONE,"Please inherit this class instead of using it directly !!!");

        this.properties = ConfigurationPropertiesBindingAssist.bindProperties(((Class<T>) type.resolve()), beanFactory,environment);
        Assert.notNull(properties,"properties must not be null !!!");
    }

    public T getProperties() {
        return properties;
    }
}


/**
 * 內部使用 ioc 容器內部的  ConfigurationPropertiesBinder ..
 */
class ConfigurationPropertiesBindingAssist {

    /**
     * 初始化标志
     */
    private static volatile boolean initFlag = false;

    private static final Object monitor = new Object();
    private final static ConfigurationPropertiesBindingPostProcessor postProcessor =
            new ConfigurationPropertiesBindingPostProcessor();

    private static void init(BeanFactory beanFactory,Environment environment) throws Exception {
        Assert.isTrue(beanFactory instanceof DefaultListableBeanFactory, "beanFactory must be DefaultListableBeanFactory !!!");
        Assert.isTrue(environment instanceof ConfigurableEnvironment, "environment must be ConfigurableEnvironment !!!");
        EmptyApplicationContext emptyApplicationContext = new EmptyApplicationContext(((DefaultListableBeanFactory) beanFactory), ((ConfigurableEnvironment) environment));
        postProcessor.setApplicationContext(emptyApplicationContext);

        // 尝试注册
        ConfigurationPropertiesBindingPostProcessor.register(emptyApplicationContext);

        postProcessor.afterPropertiesSet();
    }


    @SuppressWarnings("unchecked")
    public static <T> T bindProperties(Class<T> propertiesClass, BeanFactory beanFactory,Environment environment) {
        try {
            if (!initFlag) {
                synchronized (monitor) {
                    if (!initFlag) {
                        init(beanFactory,environment);
                        initFlag = true;
                    }
                }

            }
            return (T) postProcessor.postProcessBeforeInitialization(
                    BeanUtils.instantiateClass(propertiesClass),
                    "configurationPropertiesBindingAssist" + propertiesClass.getSimpleName()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("can't bind properties,cause is " + e.getMessage());
        }
    }
}

/**
 * empty application context
 */
class EmptyApplicationContext extends GenericApplicationContext {
    private final  ConfigurableEnvironment environment;
    public EmptyApplicationContext(DefaultListableBeanFactory beanFactory,ConfigurableEnvironment environment) {
        super(beanFactory);
        Assert.notNull(environment,"environment must not be null !!!");
        this.environment = environment;
    }

    @Override
    protected void assertBeanFactoryActive() {
        // 欺骗
    }

    @NotNull
    @Override
    public ConfigurableEnvironment getEnvironment() {
        return environment;
    }

}
