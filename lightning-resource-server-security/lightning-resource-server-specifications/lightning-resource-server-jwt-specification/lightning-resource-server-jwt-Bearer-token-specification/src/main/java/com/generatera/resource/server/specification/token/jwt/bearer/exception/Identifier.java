package com.generatera.resource.server.specification.token.jwt.bearer.exception;

import com.nimbusds.jose.shaded.json.JSONAware;
import com.nimbusds.jose.shaded.json.JSONValue;
import com.nimbusds.jose.util.Base64URL;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.*;

public class Identifier implements Serializable, Comparable<Identifier>, JSONAware {
    private static final long serialVersionUID = 365052911829193101L;
    public static final int DEFAULT_BYTE_LENGTH = 32;
    protected static final SecureRandom secureRandom = new SecureRandom();
    private final String value;

    public static List<String> toStringList(Collection<? extends Identifier> ids) {
        if (ids == null) {
            return Collections.emptyList();
        } else {
            List<String> stringList = new ArrayList(ids.size());
            Iterator var2 = ids.iterator();

            while(var2.hasNext()) {
                Identifier id = (Identifier)var2.next();
                stringList.add(id.getValue());
            }

            return stringList;
        }
    }

    public Identifier(String value) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("The value must not be null or empty string");
        } else {
            this.value = value;
        }
    }

    public Identifier(int byteLength) {
        if (byteLength < 1) {
            throw new IllegalArgumentException("The byte length must be a positive integer");
        } else {
            byte[] n = new byte[byteLength];
            secureRandom.nextBytes(n);
            this.value = Base64URL.encode(n).toString();
        }
    }

    public Identifier() {
        this(32);
    }

    public String getValue() {
        return this.value;
    }

    public String toJSONString() {
        return "\"" + JSONValue.escape(this.value) + '"';
    }

    public String toString() {
        return this.getValue();
    }

    public int compareTo(Identifier other) {
        return this.getValue().compareTo(other.getValue());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            Identifier that = (Identifier)o;
            return this.getValue() != null ? this.getValue().equals(that.getValue()) : that.getValue() == null;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.getValue() != null ? this.getValue().hashCode() : 0;
    }
}