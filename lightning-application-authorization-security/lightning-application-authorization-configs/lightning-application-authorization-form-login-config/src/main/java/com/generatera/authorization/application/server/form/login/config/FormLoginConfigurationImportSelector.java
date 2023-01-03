package com.generatera.authorization.application.server.form.login.config;

import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
import com.generatera.authorization.server.common.configuration.PropertiesBindImportSelector;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.type.AnnotationMetadata;

import java.util.LinkedList;
import java.util.List;

/**
 * 表单登陆 选择配置 ..
 */
public class FormLoginConfigurationImportSelector extends PropertiesBindImportSelector<ApplicationAuthServerProperties> {
    public FormLoginConfigurationImportSelector(BeanFactory beanFactory) {
        super(beanFactory, ApplicationAuthServerProperties.class);
    }

    @NotNull
    @Override
    public String[] selectImports(@NotNull AnnotationMetadata importingClassMetadata) {
        ApplicationAuthServerProperties properties = getProperties();
        List<String> candidates = new LinkedList<>();
        if (properties.getFormLogin().isEnable()) {
            candidates.add(ApplicationFormLoginConfiguration.FormLoginConfiguration.class.getName());
        }

        // TODO
        // token 生成的深度定制,先不处理 ...
        //if(properties.getOauth2Login().isEnable()) {
        //    // 它启用了,但是不一定有依赖 ..
        //}
        //else {
        //    candidates.add(TokenGenerateConfiguration.DefaultTokenGenerateConfiguration.class.getName());
        //}

        return candidates.size() > 0 ? candidates.toArray(String[]::new) : new String[0];
    }
}
