package com.generatera.resource.server.specification.token.jwt.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.log.LogMessage;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;

public final class JwtGrantedAuthoritiesConverter implements Converter<LightningJwt, Collection<GrantedAuthority>> {
    private final Log logger = LogFactory.getLog(this.getClass());
    private static final String DEFAULT_AUTHORITY_PREFIX = "SCOPE_";
    private static final Collection<String> WELL_KNOWN_AUTHORITIES_CLAIM_NAMES = Arrays.asList("scope", "scp");
    private String authorityPrefix = "SCOPE_";
    private String authoritiesClaimName;

    public JwtGrantedAuthoritiesConverter() {
    }

    public Collection<GrantedAuthority> convert(@Nullable LightningJwt jwt) {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        for (String authority : this.getAuthorities(jwt)) {
            grantedAuthorities.add(new SimpleGrantedAuthority(this.authorityPrefix + authority));
        }

        return grantedAuthorities;
    }

    public void setAuthorityPrefix(String authorityPrefix) {
        Assert.notNull(authorityPrefix, "authorityPrefix cannot be null");
        this.authorityPrefix = authorityPrefix;
    }

    public void setAuthoritiesClaimName(String authoritiesClaimName) {
        Assert.hasText(authoritiesClaimName, "authoritiesClaimName cannot be empty");
        this.authoritiesClaimName = authoritiesClaimName;
    }

    private String getAuthoritiesClaimName(LightningJwt jwt) {
        if (this.authoritiesClaimName != null) {
            return this.authoritiesClaimName;
        } else {
            Iterator<String> var2 = WELL_KNOWN_AUTHORITIES_CLAIM_NAMES.iterator();

            String claimName;
            do {
                if (!var2.hasNext()) {
                    return null;
                }

                claimName = var2.next();
            } while(!jwt.hasClaim(claimName));

            return claimName;
        }
    }

    private Collection<String> getAuthorities(LightningJwt jwt) {
        String claimName = this.getAuthoritiesClaimName(jwt);
        if (claimName == null) {
            this.logger.trace("Returning no authorities since could not find any claims that might contain scopes");
            return Collections.emptyList();
        } else {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(LogMessage.format("Looking for scopes in claim %s", claimName));
            }

            Object authorities = jwt.getClaim(claimName);
            if (authorities instanceof String) {
                return StringUtils.hasText((String)authorities) ? Arrays.asList(((String)authorities).split(" ")) : Collections.emptyList();
            } else {
                return authorities instanceof Collection ? this.castAuthoritiesToCollection(authorities) : Collections.emptyList();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<String> castAuthoritiesToCollection(Object authorities) {
        return (Collection<String>)authorities;
    }
}