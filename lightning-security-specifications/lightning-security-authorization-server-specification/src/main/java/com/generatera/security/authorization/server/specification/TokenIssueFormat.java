package com.generatera.security.authorization.server.specification;

import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 14:08
 * @Description Token Format
 */
public final class TokenIssueFormat implements Serializable {
    public static final TokenIssueFormat SELF_CONTAINED;
    public static final TokenIssueFormat REFERENCE;
    private final String value;

    public TokenIssueFormat(String value) {
        Assert.hasText(value, "value cannot be empty");
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            TokenIssueFormat that = (TokenIssueFormat)obj;
            return this.getValue().equals(that.getValue());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.getValue().hashCode();
    }

    static {
        SELF_CONTAINED = new TokenIssueFormat("self-contained");
        REFERENCE = new TokenIssueFormat("reference");
    }
}