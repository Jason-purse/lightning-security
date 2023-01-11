package com.generatera.security.authorization.server.specification.components.token.format.jwt.converter;

import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.support.GenericConversionService;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 10:01
 * @Description spring oauth2 copy
 *
 * claims 约定(本身也就是支持基本的类型转换)
 */
public final class ClaimConversionService extends GenericConversionService {
    private static volatile ClaimConversionService sharedInstance;

    private ClaimConversionService() {
        addConverters(this);
    }

    public static ClaimConversionService getSharedInstance() {
        ClaimConversionService sharedInstance = ClaimConversionService.sharedInstance;
        if (sharedInstance == null) {
            Class var1 = ClaimConversionService.class;
            synchronized(ClaimConversionService.class) {
                sharedInstance = ClaimConversionService.sharedInstance;
                if (sharedInstance == null) {
                    sharedInstance = new ClaimConversionService();
                    ClaimConversionService.sharedInstance = sharedInstance;
                }
            }
        }

        return sharedInstance;
    }

    public static void addConverters(ConverterRegistry converterRegistry) {
        converterRegistry.addConverter(new ObjectToStringConverter());
        converterRegistry.addConverter(new ObjectToBooleanConverter());
        converterRegistry.addConverter(new ObjectToInstantConverter());
        converterRegistry.addConverter(new ObjectToURLConverter());
        converterRegistry.addConverter(new ObjectToListStringConverter());
        converterRegistry.addConverter(new ObjectToMapStringObjectConverter());
    }
}