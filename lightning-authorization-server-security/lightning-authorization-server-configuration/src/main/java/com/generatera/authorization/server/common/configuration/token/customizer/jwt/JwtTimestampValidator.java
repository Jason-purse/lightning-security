package com.generatera.authorization.server.common.configuration.token.customizer.jwt;

import com.generatera.authorization.server.common.configuration.token.LightningAuthError;
import com.generatera.authorization.server.common.configuration.token.LightningTokenValidator;
import com.generatera.authorization.server.common.configuration.token.LightningTokenValidatorResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class JwtTimestampValidator implements LightningTokenValidator<LightningJwt> {
    private final Log logger;
    private static final Duration DEFAULT_MAX_CLOCK_SKEW;
    private final Duration clockSkew;
    private Clock clock;

    public JwtTimestampValidator() {
        this(DEFAULT_MAX_CLOCK_SKEW);
    }

    public JwtTimestampValidator(Duration clockSkew) {
        this.logger = LogFactory.getLog(this.getClass());
        this.clock = Clock.systemUTC();
        Assert.notNull(clockSkew, "clockSkew cannot be null");
        this.clockSkew = clockSkew;
    }

    public LightningTokenValidatorResult validate(LightningJwt jwt) {
        Assert.notNull(jwt, "jwt cannot be null");
        Instant expiry = jwt.getExpiresAt();
        if (expiry != null && Instant.now(this.clock).minus(this.clockSkew).isAfter(expiry)) {
            LightningAuthError oAuth2Error = this.createOAuthError(String.format("Jwt expired at %s", jwt.getExpiresAt()));
            return LightningTokenValidatorResult.failure(new LightningAuthError[]{oAuth2Error});
        } else {
            Instant notBefore = jwt.getNotBefore();
            if (notBefore != null && Instant.now(this.clock).plus(this.clockSkew).isBefore(notBefore)) {
                LightningAuthError oAuth2Error = this.createOAuthError(String.format("Jwt used before %s", jwt.getNotBefore()));
                return LightningTokenValidatorResult.failure(new LightningAuthError[]{oAuth2Error});
            } else {
                return LightningTokenValidatorResult.success();
            }
        }
    }

    private LightningAuthError createOAuthError(String reason) {
        this.logger.debug(reason);
        return new LightningAuthError("invalid_token", reason, "https://tools.ietf.org/html/rfc6750#section-3.1");
    }

    public void setClock(Clock clock) {
        Assert.notNull(clock, "clock cannot be null");
        this.clock = clock;
    }

    static {
        DEFAULT_MAX_CLOCK_SKEW = Duration.of(60L, ChronoUnit.SECONDS);
    }
}