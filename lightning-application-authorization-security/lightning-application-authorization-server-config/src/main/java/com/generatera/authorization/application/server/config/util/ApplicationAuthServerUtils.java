package com.generatera.authorization.application.server.config.util;

import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettingProperties;
import com.jianyue.lightning.boot.starter.util.BeanUtils;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import com.jianyue.lightning.boot.starter.util.OptionalFlux;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.util.Assert;

import java.util.function.Supplier;

import static com.generatera.authorization.application.server.config.util.StringUtils.normalize;
import static com.jianyue.lightning.boot.starter.util.SwitchUtil.switchFunc;

/**
 * @author FLJ
 * @date 2023/1/31
 * @time 17:20
 * @Description 帮助器 ...
 */

public class ApplicationAuthServerUtils {

    private final ApplicationAuthServerProperties properties;

    private final ApplicationAuthServerProperties fullConfigProperties;

    public ApplicationAuthServerUtils(ApplicationAuthServerProperties properties) {
        this.properties = properties;
        this.fullConfigProperties = new ApplicationAuthServerProperties();
        // 初始化
        init();
    }

    private void init() {

        OptionalFlux
                .of(properties.getAppAuthPrefix())
                .consume(
                        switchFunc(
                        org.springframework.util.StringUtils::hasText,
                        prefix -> properties.setAppAuthPrefix(org.springframework.util.StringUtils.trimTrailingCharacter(normalize(prefix),'/')))
                );

        ProviderSettingProperties providerSettingProperties = this.properties.getProviderSettingProperties();
        ProviderSettingProperties fullConfigPropertiesProviderSettingProperties = this.fullConfigProperties.getProviderSettingProperties();

        if(org.springframework.util.StringUtils.hasText(providerSettingProperties.getIssuer())) {
            providerSettingProperties.setIssuer(normalize(providerSettingProperties.getIssuer()));
            fullConfigPropertiesProviderSettingProperties.setIssuer(providerSettingProperties.getIssuer());
        }


        // token 才需要 特殊处理 ...
        providerSettingProperties.setJwkSetEndpoint(normalize(ElvisUtil.stringElvis(providerSettingProperties.getJwkSetEndpoint(), ProviderSettingProperties.JWT_SET_ENDPOINT)));
        providerSettingProperties.setTokenEndpoint(normalize(ElvisUtil.stringElvis(providerSettingProperties.getTokenEndpoint(),ProviderSettingProperties.TOKEN_ENDPOINT)));
        providerSettingProperties.setTokenRevocationEndpoint(normalize(ElvisUtil.stringElvis(providerSettingProperties.getTokenRevocationEndpoint(),ProviderSettingProperties.TOKEN_REVOCATION_ENDPOINT)));
        providerSettingProperties.setTokenIntrospectionEndpoint(normalize(ElvisUtil.stringElvis(providerSettingProperties.getTokenIntrospectionEndpoint(),ProviderSettingProperties.TOKEN_INTROSPECTION_ENDPOINT)));



        ApplicationAuthServerProperties.ServerMetaDataEndpointConfig endpointConfig = properties.getServerMetaDataEndpointConfig();
        endpointConfig.setOpenConnectIdMetadataEndpointUri(
                normalize(ElvisUtil.stringElvis(properties.getServerMetaDataEndpointConfig().getOpenConnectIdMetadataEndpointUri(),
                ApplicationAuthServerProperties.ServerMetaDataEndpointConfig.OPEN_CONNECT_ID_METADATA_ENDPOINT))
        );

        ApplicationAuthServerProperties.BackendSeparation backendSeparation = this.properties.getBackendSeparation();
        backendSeparation.setLogoutProcessUrl(normalize(ElvisUtil.stringElvis(backendSeparation.getLogoutProcessUrl(), ApplicationAuthServerProperties.BackendSeparation.DEFAULT_LOGOUT_PROCESS_URL)));

        ApplicationAuthServerProperties.NoSeparation noSeparation = this.properties.getNoSeparation();
        noSeparation.setLoginPageUrl(normalize(ElvisUtil.stringElvis(noSeparation.getLoginPageUrl(), ApplicationAuthServerProperties.NoSeparation.DEFAULT_LOGIN_PAGE_URL)));
        noSeparation.setLogoutPageUrl(normalize(ElvisUtil.stringElvis(noSeparation.getLogoutPageUrl(), ApplicationAuthServerProperties.NoSeparation.DEFAULT_LOGOUT_PAGE_URL)));
        noSeparation.setLogoutSuccessUrl(normalize(ElvisUtil.stringElvis(noSeparation.getLogoutSuccessUrl(), ApplicationAuthServerProperties.NoSeparation.DEFAULT_LOGOUT_SUCCESS_URL)));
        noSeparation.setFailureForwardOrRedirectUrl(normalize(ElvisUtil.stringElvis(noSeparation.getFailureForwardOrRedirectUrl(), ApplicationAuthServerProperties.NoSeparation.DEFAULT_FAILURE_FORWARD_OR_REDIRECT_URL)));

        ElvisUtil.isNotEmptyConsumer(noSeparation.getLogoutProcessUrl(),processUrl -> {
            noSeparation.setLogoutProcessUrl(normalize(processUrl));
        });
        ElvisUtil.isNotEmptyConsumer(noSeparation.getDefaultSuccessUrl(),url -> {
            noSeparation.setDefaultSuccessUrl(normalize(url));
        });
        ElvisUtil.isNotEmptyConsumer(noSeparation.getSuccessForwardOrRedirectUrl(),url -> {
            noSeparation.setSuccessForwardOrRedirectUrl(normalize(url));
        });


        BeanUtils.updateProperties(properties,fullConfigProperties);

        // 也需要特殊处理 ..
        ApplicationAuthServerProperties.ServerMetaDataEndpointConfig fullConfigPropertiesServerMetaDataEndpointConfig = this.fullConfigProperties.getServerMetaDataEndpointConfig();
        fullConfigPropertiesProviderSettingProperties.setJwkSetEndpoint(normalApplicationAuthServerOfUrl(providerSettingProperties::getJwkSetEndpoint));
        fullConfigPropertiesProviderSettingProperties.setTokenEndpoint(normalApplicationAuthServerOfUrl(providerSettingProperties::getTokenEndpoint));
        fullConfigPropertiesProviderSettingProperties.setTokenRevocationEndpoint(normalApplicationAuthServerOfUrl(providerSettingProperties::getTokenRevocationEndpoint));
        fullConfigPropertiesProviderSettingProperties.setTokenIntrospectionEndpoint(normalApplicationAuthServerOfUrl(providerSettingProperties::getTokenIntrospectionEndpoint));
        fullConfigPropertiesServerMetaDataEndpointConfig.setOpenConnectIdMetadataEndpointUri(normalApplicationAuthServerOfUrl(endpointConfig::getOpenConnectIdMetadataEndpointUri));

    }

    private String normalApplicationAuthServerOfUrl(Supplier<String> function) {
        String url = function.get();
        return properties.getAppAuthPrefix() + url;
    }

    public ApplicationAuthServerProperties getFullConfigProperties() {
        return fullConfigProperties;
    }

    public ApplicationAuthServerProperties getProperties() {
        return properties;
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
