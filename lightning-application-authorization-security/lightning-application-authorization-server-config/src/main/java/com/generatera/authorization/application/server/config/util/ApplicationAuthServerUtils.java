package com.generatera.authorization.application.server.config.util;

import com.generatera.authorization.application.server.config.AppAuthConfigConstant;
import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettingProperties;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.util.Assert;

import static com.generatera.authorization.application.server.config.util.StringUtils.normalize;

/**
 * @author FLJ
 * @date 2023/1/31
 * @time 17:20
 * @Description 帮助器 ...
 */

public class ApplicationAuthServerUtils {

    private final ApplicationAuthServerProperties properties;

    public ApplicationAuthServerUtils(ApplicationAuthServerProperties properties) {
        this.properties = properties;
        // 初始化
        init();
    }

    private void init() {
        this.properties.setAppAuthPrefix(normalize(ElvisUtil.stringElvis(properties.getAppAuthPrefix(), AppAuthConfigConstant.APP_AUTH_SERVER_PREFIX),false));
        ProviderSettingProperties providerSettingProperties = this.properties.getProviderSettingProperties();
        if(org.springframework.util.StringUtils.hasText(providerSettingProperties.getIssuer())) {
            providerSettingProperties.setIssuer(normalize(providerSettingProperties.getIssuer()));
        }
         = ElvisUtil.stringElvis(providerSettingProperties.getJwkSetEndpoint(), ProviderSettingProperties.JWT_SET_ENDPOINT);
        this.properties.getServerMetaDataEndpointConfig().setOpenConnectIdMetadataEndpointUri(ElvisUtil.stringElvis(properties.getServerMetaDataEndpointConfig().getOpenConnectIdMetadataEndpointUri(),
                ApplicationAuthServerProperties.ServerMetaDataEndpointConfig.OPEN_CONNECT_ID_METADATA_ENDPOINT));


    }

    public ApplicationAuthServerProperties getApplicationAuthServerProperties() {
        return this.properties;
    }


    public static <B extends HttpSecurityBuilder<B>> ApplicationAuthServerUtils getApplicationAuthServerProperties(B builder) {
        ApplicationAuthServerUtils sharedObject = builder.getSharedObject(ApplicationAuthServerUtils.class);
        if (sharedObject == null) {
            ApplicationAuthServerProperties sharedObject1 = builder.getSharedObject(ApplicationAuthServerProperties.class);
            Assert.notNull(sharedObject1, "applicationAuthServerProperties must not be null !!!");
            sharedObject = new ApplicationAuthServerUtils(sharedObject1);
            builder.setSharedObject(ApplicationAuthServerUtils.class, sharedObject);
        }
        return sharedObject;
    }
}
