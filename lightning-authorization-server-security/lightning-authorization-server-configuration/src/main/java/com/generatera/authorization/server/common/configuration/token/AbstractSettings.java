package com.generatera.authorization.server.common.configuration.token;

import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 抽象配置 .. 类似于map
 */
public abstract class AbstractSettings implements Serializable {
    private final Map<String, Object> settings;

    protected AbstractSettings(Map<String, Object> settings) {
        Assert.notEmpty(settings, "settings cannot be empty");
        this.settings = Map.copyOf(settings);
    }

    public <T> T getSetting(String name) {
        Assert.hasText(name, "name cannot be empty");
        return (T)this.getSettings().get(name);
    }

    public Map<String, Object> getSettings() {
        return this.settings;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            AbstractSettings that = (AbstractSettings)obj;
            return this.settings.equals(that.settings);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(this.settings);
    }

    public String toString() {
        return "AbstractSettings {settings=" + this.settings + "}";
    }



    protected abstract static class AbstractBuilder<T extends AbstractSettings, B extends AbstractSettings.AbstractBuilder<T, B>> {
        private final Map<String, Object> settings = new HashMap<>();

        protected AbstractBuilder() {
        }

        public B setting(String name, Object value) {
            Assert.hasText(name, "name cannot be empty");
            Assert.notNull(value, "value cannot be null");
            this.getSettings().put(name, value);
            return this.getThis();
        }

        public B settings(Consumer<Map<String, Object>> settingsConsumer) {
            settingsConsumer.accept(this.getSettings());
            return this.getThis();
        }

        public abstract T build();

        protected final Map<String, Object> getSettings() {
            return this.settings;
        }

        protected final B getThis() {
            return (B)this;
        }
    }
}