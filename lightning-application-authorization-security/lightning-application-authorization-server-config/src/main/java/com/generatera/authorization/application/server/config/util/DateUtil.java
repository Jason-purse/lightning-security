package com.generatera.authorization.application.server.config.util;

import java.time.Instant;

public class DateUtil {
    public static Instant convertTo(Object millisOrInstant) {
        if(millisOrInstant instanceof Long millis) {
            return Instant.ofEpochMilli(millis);
        }
        else {
            return ((Instant) millisOrInstant);
        }
    }
}
