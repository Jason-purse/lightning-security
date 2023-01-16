package com.generatera.security.authorization.server.specification.components.token.format.jwt;

import com.generatera.security.authorization.server.specification.components.token.format.jwt.converter.ClaimConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.Assert;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;
/**
 * @author FLJ
 * @date 2023/1/16
 * @time 14:21
 * @Description claim 访问器 ...
 */
public interface ClaimAccessor {

    Map<String, Object> getClaims();

    @SuppressWarnings("unchecked")
    default <T> T getClaim(String claim) {
        return !this.hasClaim(claim) ? null : (T)this.getClaims().get(claim);
    }

    default boolean hasClaim(String claim) {
        Assert.notNull(claim, "claim cannot be null");
        return this.getClaims().containsKey(claim);
    }

    /** @deprecated */
    @Deprecated
    default Boolean containsClaim(String claim) {
        return this.hasClaim(claim);
    }

    default String getClaimAsString(String claim) {
        return !this.hasClaim(claim) ? null : (String) ClaimConversionService.getSharedInstance().convert(this.getClaims().get(claim), String.class);
    }

    default Boolean getClaimAsBoolean(String claim) {
        if (!this.hasClaim(claim)) {
            return null;
        } else {
            Object claimValue = this.getClaims().get(claim);
            Boolean convertedValue = (Boolean) ClaimConversionService.getSharedInstance().convert(claimValue, Boolean.class);
            Assert.notNull(convertedValue, () -> {
                return "Unable to convert claim '" + claim + "' of type '" + claimValue.getClass() + "' to Boolean.";
            });
            return convertedValue;
        }
    }

    default Instant getClaimAsInstant(String claim) {
        if (!this.hasClaim(claim)) {
            return null;
        } else {
            Object claimValue = this.getClaims().get(claim);
            Instant convertedValue = (Instant)ClaimConversionService.getSharedInstance().convert(claimValue, Instant.class);
            Assert.isTrue(convertedValue != null, () -> {
                return "Unable to convert claim '" + claim + "' of type '" + claimValue.getClass() + "' to Instant.";
            });
            return convertedValue;
        }
    }

    default URL getClaimAsURL(String claim) {
        if (!this.hasClaim(claim)) {
            return null;
        } else {
            Object claimValue = this.getClaims().get(claim);
            URL convertedValue = (URL)ClaimConversionService.getSharedInstance().convert(claimValue, URL.class);
            Assert.isTrue(convertedValue != null, () -> {
                return "Unable to convert claim '" + claim + "' of type '" + claimValue.getClass() + "' to URL.";
            });
            return convertedValue;
        }
    }

    default Map<String, Object> getClaimAsMap(String claim) {
        if (!this.hasClaim(claim)) {
            return null;
        } else {
            TypeDescriptor sourceDescriptor = TypeDescriptor.valueOf(Object.class);
            TypeDescriptor targetDescriptor = TypeDescriptor.map(Map.class, TypeDescriptor.valueOf(String.class), TypeDescriptor.valueOf(Object.class));
            Object claimValue = this.getClaims().get(claim);
            Map<String, Object> convertedValue = (Map<String,Object>)ClaimConversionService.getSharedInstance().convert(claimValue, sourceDescriptor, targetDescriptor);
            Assert.isTrue(convertedValue != null, () -> {
                return "Unable to convert claim '" + claim + "' of type '" + claimValue.getClass() + "' to Map.";
            });
            return convertedValue;
        }
    }

    default List<String> getClaimAsStringList(String claim) {
        if (!this.hasClaim(claim)) {
            return null;
        } else {
            TypeDescriptor sourceDescriptor = TypeDescriptor.valueOf(Object.class);
            TypeDescriptor targetDescriptor = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(String.class));
            Object claimValue = this.getClaims().get(claim);
            List<String> convertedValue = (List<String>)ClaimConversionService.getSharedInstance().convert(claimValue, sourceDescriptor, targetDescriptor);
            Assert.isTrue(convertedValue != null, () -> {
                return "Unable to convert claim '" + claim + "' of type '" + claimValue.getClass() + "' to List.";
            });
            return convertedValue;
        }
    }
}